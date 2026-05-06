package uk.gov.hmcts.reform.LandA.performance.scenarios.utils

import io.gatling.core.structure.ChainBuilder
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object LoginFlow {

  def login(groupName: String, successText: String): ChainBuilder =
    group(groupName) {
      exec(http("LAU Login Email")
        .post(Environment.idamUrl + "/enter-email")
        .headers(CommonHeader.navigation_headers)
        .formParam("email", "#{email}")
        .formParam("_csrf", "#{csrfToken}")
        .check(css("input[name='_csrf']", "value").saveAs("csrfToken"))
        .check(css("input[name='password']", "name").is("password")))
      .exec(http("LAU Login Password")
        .post(Environment.idamUrl + "/enter-password")
        .headers(CommonHeader.navigation_headers)
        .formParam("action", "_submit")
        .formParam("password", "#{password}")
        .formParam("_csrf", "#{csrfToken}")
        .check(substring(successText)))
    }.pause(Environment.thinkTime)
}