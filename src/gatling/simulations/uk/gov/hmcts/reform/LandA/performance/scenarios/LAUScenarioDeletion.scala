package uk.gov.hmcts.reform.LandA.performance.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Environment, CommonHeader, LoginFlow}

object LAUScenarioDeletion {

  val BaseURL = Environment.baseUrl
  val IdamURL = Environment.idamUrl

  val ThinkTime = Environment.thinkTime

  val Users = csv("UsersDeletion.csv").circular
  val CaseAuditSearches = csv("CaseAuditSearch.csv").circular
  val CaseDeleteSearches = csv("CaseDeletionSearch.csv").circular
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

  val LAULoginDeletion =

    doSwitch("#{env}")(
      "perftest" -> feed(Users),
      "aat" -> feed(UsersAat)
    )
      .exec(LoginFlow.login("LAU_120_Deletion_Login", "Case Deletions Search"))

  //Perform a case audit search and download the CSV file
  val LAUCaseDeletionSearch =

    doSwitch("#{env}")(
      "perftest" -> feed(CaseDeleteSearches),
      "aat" -> feed(CaseAuditSearchesAat)
    )
      .group("LAU_130_CaseDeletionSearch") {
        exec(http("LAU Case Deletion Search")
          .post(BaseURL + "/case-deletions-search")
          .headers(CommonHeader.navigation_headers)
          .header("accept-language", "en-GB,en-US;q=0.9,en;q=0.8")
          .formParam("caseRef", "")
          .formParam("caseTypeId", "")
          .formParam("caseJurisdictionId", "cmc")
          .formParam("startTimestamp", "#{caseStartTimestamp}")
          .formParam("endTimestamp", "#{caseEndTimestamp}")
          .formParam("page", "1")
          .formParam("_csrf", "#{csrfToken}")
          .check(substring("Case Deletions Results"))
          .check(regex("""Case Deletions Results</li>(?s)\s*?<p class="govuk-body">No results found""").optional.saveAs("noCaseResults"))
          .check(substring("case-deletions-next-btn").saveAs("moreCasePages")))

      }
      .pause(ThinkTime)



      //only continue if results were found ('No results found' wasn't found on the results page)
      .doIf("#{noCaseResults.isUndefined()}") {

        //only load the second page if there are more pages available
        doIf("#{moreCasePages.exists()}") {

          group("LAU_140_CaseDeletionPage2") {
            exec(http("LAU Case Deletion Page 2")
              .get(BaseURL + "/case-deletions/page/2")
              .headers(CommonHeader.navigation_headers)
              .check(substring("Page 2")))
          }
            .pause(ThinkTime)
        }
      }


      .group("LAU_150_CaseDeletionDownload") {
        exec(http("Case Deletion CSV Download")
          .get(BaseURL + "/case-deletions/csv")
          .headers(CommonHeader.download_headers)
          .check(substring("Case Jurisdiction Id"))
          .check(substring("filename")))
      }
      .pause(ThinkTime)


  val LAUSignOut =

    group("LAU_160_SignOut") {
      exec(http("User Sign Out")
        .get(BaseURL + "/logout")
        .headers(CommonHeader.navigation_headers)
        .check(substring("Sign in")))
    }
      .pause(ThinkTime)


}
