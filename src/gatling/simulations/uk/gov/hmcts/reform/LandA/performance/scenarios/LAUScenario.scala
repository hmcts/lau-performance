package uk.gov.hmcts.reform.LandA.performance.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{CommonHeader, Environment, LoginFlow}

object LAUScenario {

  val BaseURL = Environment.baseUrl
  val IdamURL = Environment.idamUrl

  val ThinkTime = Environment.thinkTime

  val Users = csv("Users.csv").circular
  val CaseAuditSearches = csv("CaseAuditSearch.csv").circular
  val LogonAuditSearches = csv("LogonAuditSearch.csv").circular
  val UsersAat = csv("UsersAat.csv").circular
  val CaseAuditSearchesAat = csv("CaseAuditSearchAat.csv").circular
  val LogonAuditSearchesAat = csv("LogonAuditSearchAat.csv").circular


  val LAUHomepage =

    group("LAU_020_Homepage") {
      exec(http("LAU Homepage")
        .get(BaseURL)
        .headers(CommonHeader.homepage_header)
        .check(css("input[name='email']", "name").is("email"))
        .check(css("input[name='_csrf']", "value").saveAs("csrfToken")))
    }
      .pause(ThinkTime)

  val LAULogin =

    doSwitch("#{env}") (
      "perftest" -> feed(Users),
      "aat" -> feed(UsersAat)
    )
    .exec(LoginFlow.login("LAU_030_Login", "Case audit"))


  val RetrieveCsrfToken =

    group("LAU_Csrf_Token") {
      exec(http("LAU_Csrf_Token")
        .get(BaseURL + "/csrf-token")
        .headers(CommonHeader.navigation_headers)
        .check(jsonPath("$.csrfToken").saveAs("csrfToken")))
    }
      .pause(ThinkTime)

  //Perform a case audit search and download the CSV file
  val LAUCaseAuditSearch =

    doSwitch("#{env}") (
      "perftest" -> feed(CaseAuditSearches),
      "aat" -> feed(CaseAuditSearchesAat)
    )
      .group("LAU_040_CaseAuditSearch") {
        exec(http("LAU Case audit")
          .post(BaseURL + "/case-search")
          .headers(CommonHeader.navigation_headers)
          .formParam("_csrf", "#{csrfToken}")
          .formParam("userId", "#{userID}")
          .formParam("caseRef", "")
          .formParam("startTimestamp", "#{caseStartTimestamp}")
          .formParam("caseTypeId", "")
          .formParam("caseJurisdictionId", "#{caseJurisdictionId}")
          .formParam("endTimestamp", "#{caseEndTimestamp}")
          .formParam("page", "1")
          .check(substring("Case activity results"))
          .check(regex("""Case activity results</li>(?s)\s*?<p class="govuk-body">No results found""").optional.saveAs("noCaseResults"))
          .check(substring("case-activity-next-btn").optional.saveAs("moreCasePages")))
      }
      .pause(ThinkTime)

      //only continue if results were found ('No results found' wasn't found on the results page)
      .doIf("#{noCaseResults.isUndefined()}") {

        //only load the second page if there are more pages available
        doIf("#{moreCasePages.exists()}") {

          group("LAU_050_CaseAuditPage2") {
            exec(http("LAU Case Audit Page 2")
              .get(BaseURL + "/case-activity/page/2")
              .headers(CommonHeader.navigation_headers)
              .check(substring("Page 2")))
          }
            .pause(ThinkTime)

        }

          .group("LAU_060_CaseActivityDownload") {
            exec(http("Case Activity CSV Download")
              .get(BaseURL + "/case-activity/csv")
              .headers(CommonHeader.download_headers)
              .check(substring("Case Jurisdiction Id"))
              .check(substring("filename")))
          }
          .pause(ThinkTime)

          .group("LAU_070_CaseSearchDownload") {
            exec(http("Case Search CSV Download")
              .get(BaseURL + "/case-searches/csv")
              .headers(CommonHeader.download_headers)
              .check(substring("Case Ref"))
              .check(substring("filename")))
          }
          .pause(ThinkTime)

      }

  //Perform a logon audit search and download the CSV file
  val LogonsAuditSearch =

    doSwitch("#{env}") (
      "perftest" -> feed(LogonAuditSearches),
      "aat" -> feed(LogonAuditSearchesAat)
    )
      .group("LAU_080_LogonAuditSearch") {
        exec(http("LAU Logon Audit Search")
          .post(BaseURL + "/logon-search")
          .headers(CommonHeader.navigation_headers)
          .formParam("userId", "")
          .formParam("emailAddress", "#{logonEmailAddress}")
          .formParam("startTimestamp", "#{logonStartTimestamp}")
          .formParam("endTimestamp", "#{logonEndTimestamp}")
          .formParam("page", "1")
          .formParam("_csrf", "#{csrfToken}")
          .check(regex("Results|System Logon Results"))
          .check(regex("""System Logon Results</h2>(?s)\s*?<p class="govuk-body">No results found""").optional.saveAs("noLogonResults"))
          .check(substring("logons-next-btn").optional.saveAs("moreLogonPages")))
      }
      .pause(Environment.thinkTime)

      //only continue if results were found ('No results found' wasn't found on the results page)
      .doIf("#{noLogonResults.isUndefined()}") {

        //only load the second page if there are more pages available
        doIf("#{moreLogonPages.exists()}") {

          group("LAU_090_LogonAuditPage2") {
            exec(http("LAU Logon Audit Page 2")
              .get(BaseURL + "/logons/page/2")
              .headers(CommonHeader.navigation_headers)
              .check(substring("Page 2")))
          }
            .pause(ThinkTime)

        }

          .group("LAU_100_LogonActivityDownload") {
            exec(http("Logon Activity CSV Download")
              .get(BaseURL + "/logons/csv")
              .headers(CommonHeader.download_headers)
              .check(substring("Ip Address"))
              .check(substring("filename")))

          }
          .pause(Environment.thinkTime)

      }

}
