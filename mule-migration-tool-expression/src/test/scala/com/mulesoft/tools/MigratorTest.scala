package com.mulesoft.tools

import org.scalatest.{FlatSpec, Matchers}

class MigratorTest extends FlatSpec with Matchers {
  "Migrator" should "migrate a map" in {
    Migrator.migrate(" [ \"a\" : [1,2], \"b\" : 666  ]   ") shouldBe "---\n{\n  \"a\": [1, 2],\n  \"b\": 666\n}"
  }

  "Migrator" should "migrate a map with identifiers" in {
    Migrator.migrate("['user-agent': 'Mule 3.8.0']") shouldBe "---\n{\n  \"user-agent\": 'Mule 3.8.0'\n}"
  }

  it should "migrate a list of lists" in {
    Migrator.migrate("[[3,5,7],[]]") shouldBe "---\n[\n  [3, 5, 7],\n  []\n]"
  }

  it should "migrate an empty list" in {
    Migrator.migrate("[]") shouldBe "---\n[]"
  }

  it should "migrate an number" in {
    Migrator.migrate("1.2") shouldBe "---\n1.2"
  }

  it should "migrate a negative number" in {
    Migrator.migrate("-1.2") shouldBe "---\n-1.2"
  }

  it should "migrate a dot expression" in {
    Migrator.migrate("payload.foo") shouldBe "---\npayload.foo"
  }

  it should "migrate a subscript expression" in {
    Migrator.migrate("message.inboundProperties['http.query.params']") shouldBe "---\nmessage.inboundProperties['http.query.params']"
  }

  it should "migrate a subscript expression without binding context" in {
    Migrator.migrate("lala.pepe") shouldBe "---\nvars.lala.pepe"
  }

  it should "migrate a not equals expression" in {
    Migrator.migrate("payload != null") shouldBe "---\npayload != null"
  }

  it should "migrate a equals expression" in {
    Migrator.migrate("payload == null") shouldBe "---\npayload == null"
  }

  it should "migrate a less than expression" in {
    Migrator.migrate("payload.bla < 5 + 3") shouldBe "---\npayload.bla < 5 + 3"
  }

  it should "migrate a greater than expression" in {
    Migrator.migrate("4 + 7 > 5 + 3") shouldBe "---\n4 + 7 > 5 + 3"
  }

  it should "migrate a greater than or equals expression" in {
    Migrator.migrate("4 >= 2 + 2") shouldBe "---\n4 >= 2 + 2"
  }

  it should "migrate a smaller than or equals expression" in {
    Migrator.migrate("2 + 2 <= 4") shouldBe "---\n2 + 2 <= 4"
  }

  it should "migrate an and expression" in {
    Migrator.migrate("true && true == true") shouldBe "---\ntrue and true == true"
  }

  it should "migrate an or expression" in {
    Migrator.migrate("true || true == true") shouldBe "---\ntrue or true == true"
  }

  it should "migrate a logical expression" in {
    Migrator.migrate("true || false && true == true") shouldBe "---\ntrue or false and true == true"
  }

  it should "migrate a logical complex expression" in {
    Migrator.migrate("1+1 == 2 || true == true") shouldBe "---\n1 + 1 == 2 or true == true"
  }

  it should "migrate a logical complex expression with identifier" in {
    Migrator.migrate("payload.foo == 2 || true == true") shouldBe "---\npayload.foo == 2 or true == true"
  }

  it should "migrate an expression containing NullPayload.getInstance()" in {
    Migrator.migrate("payload != NullPayload.getInstance()") shouldBe "---\npayload != null"
  }

  it should "migrate a simple addition/subtraction expression" in {
    Migrator.migrate("-2 - 3 + 1") shouldBe "---\n-2 - 3 + 1"
  }

  it should "migrate multiplication" in {
    Migrator.migrate("1*2") shouldBe "---\n1 * 2"
  }

  it should "migrate multiple multiplication" in {
    Migrator.migrate("1*2*3") shouldBe "---\n1 * 2 * 3"
  }

  it should "migrate multiplication and addition" in {
    Migrator.migrate("1*2+3") shouldBe "---\n1 * 2 + 3"
  }

  it should "migrate addition and multiplication and division" in {
    Migrator.migrate("1+2*3/4 + 5") shouldBe "---\n1 + 2 * 3 / 4 + 5"
  }

  it should "migrate arithmetic with comparison 1" in {
    Migrator.migrate("1+2*3/4 + 5 ==7.5") shouldBe "---\n1 + 2 * 3 / 4 + 5 == 7.5"
  }

  it should "migrate arithmetic with comparison 2" in {
    Migrator.migrate("1+2*3/4 + 5 ==15/2") shouldBe "---\n1 + 2 * 3 / 4 + 5 == 15 / 2"
  }
}