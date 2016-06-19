package uscala.result

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import uscala.result.Result.{Ok, Fail}

import scala.util.{Success, Failure}

class ResultSpec extends Specification with ScalaCheck {

  def f(n: Int) = n + 1
  def fa(n: Int) = f(n)
  def fb(n: Int) = n + 2

  "fold" >> {
    "should apply fa if he result is Fail" >> prop { n: Int =>
      Fail(n).fold(fa, fb) must_=== fa(n)
    }
    "should apply fb if he result is Ok" >> prop { n: Int =>
      Ok(n).fold(fa, fb) must_=== fb(n)
    }
  }

  "map" >> {
    "should not apply f if he result is Fail" >> prop { n: Int =>
      Fail(n).map(f) must_=== Fail(n)
    }
    "should apply f if he result is Ok" >> prop { n: Int =>
      Ok(n).map(f) must_=== Ok(f(n))
    }
  }

  "leftMap" >> {
    "should apply f if he result is Fail" >> prop { n: Int =>
      Fail(n).leftMap(f) must_=== Fail(f(n))
    }
    "should not apply f if he result is Ok" >> prop { n: Int =>
      Ok(n).leftMap(f) must_=== Ok(n)
    }
  }

  "mapOk" >> {
    "should be an alias of map" >> prop { n: Int =>
      Ok(n).mapOk(f) must_=== Ok(n).map(f)
      Fail(n).mapOk(f) must_=== Fail(n).map(f)
    }
  }

  "mapFail" >> {
    "should be an alias of leftMap" >> prop { n: Int =>
      Ok(n).mapFail(f) must_=== Ok(n).leftMap(f)
      Fail(n).mapFail(f) must_=== Fail(n).leftMap(f)
    }
  }

  "bimap" >> {
    "should apply fa if he result is Fail" >> prop { n: Int =>
      Fail(n).bimap(fa, fb) must_=== Fail(fa(n))
    }
    "should apply fb if he result is Ok" >> prop { n: Int =>
      Ok(n).bimap(fa, fb) must_=== Ok(fb(n))
    }
  }

  "swap" >> {
    "should move Fail value to Ok" >> prop { n: Int =>
      Fail(n).swap must_=== Ok(n)
    }
    "should move Ok value to Fail" >> prop { n: Int =>
      Ok(n).swap must_=== Fail(n)
    }
  }

  "merge" >> {
    "should return the value of Fail if it's a Fail" >> prop { n: Int =>
      Fail(n).merge must_=== n
    }
    "should return the value of Ok if it's an Ok" >> prop { n: Int =>
      Ok(n).merge must_=== n
    }
  }

  "foreach" >> {
    "should not apply f if it's a Fail" >> prop { n: Int =>
      var effect: Option[Int] = None
      def sideEffect(i: Int): Unit = effect = Some(i)
      Fail(n).foreach(sideEffect)
      effect must beNone
    }
    "should apply f if it's an Ok" >> prop { n: Int =>
      var effect: Option[Int] = None
      def sideEffect(i: Int): Unit = effect = Some(i)
      Ok(n).foreach(sideEffect)
      effect must beSome(n)
    }
  }

  "getOrElse" >> {
    "should return the default if it's a Fail" >> prop { (n1: Int, n2: Int) =>
      Fail(n1).getOrElse(n2) must_=== n2
    }
    "should return the ok value it's an Ok" >> prop { (n1: Int, n2: Int) =>
      Ok(n1).getOrElse(n2) must_=== n1
    }
  }

  "orElse" >> {
    "should return the fallback result if it's a Fail" >> prop { (n1: Int, n2: Int) =>
      Fail(n1).orElse(Ok(n2)) must_=== Ok(n2)
    }
    "should return the ok value it's an Ok" >> prop { (n1: Int, n2: Int) =>
      Ok(n1).orElse(Ok(n2)) must_=== Ok(n1)
    }
  }

  "recover" >> {
    "should recover if it's a Fail and the function is defined for the value" >> prop { n: Int =>
      val recovered = Fail(n).recover {
        case i if i % 2 == 0 => i
      }
      if (n % 2 == 0) recovered must_=== Ok(n)
      else recovered must_=== Fail(n)
    }
    "should ignore it if it's an Ok" >> prop { n: Int =>
      Ok(n).recover {
       case _ => 1
      } must_=== Ok(n)
    }
  }

  "recoverWith" >> {
    "should recover if it's a Fail and the function is defined for the value" >> prop { n: Int =>
      val recovered = Fail(n).recoverWith {
        case i if i % 2 == 0 => Ok(i)
        case i if i % 3 == 0 => Fail(i)
      }
      if (n % 2 == 0) recovered must_=== Ok(n)
      else if (n % 3 == 0) recovered must_=== Fail(n)
      else recovered must_=== Fail(n)
    }
    "should ignore it if it's an Ok" >> prop { n: Int =>
      Ok(n).recover {
       case _ => 1
      } must_=== Ok(n)
    }
  }

  "toEither" >> {
    "should put the Fail value on the left" >> prop { n: Int =>
      Fail(n).toEither must beLeft(n)
    }
    "should put the Ok value on the right" >> prop { n: Int =>
      Ok(n).toEither must beRight(n)
    }
  }

  "toOption" >> {
    "should return None if it's a Fail" >> prop { n: Int =>
      Fail(n).toOption must beNone
    }
    "should return Some(value) if it's an Ok" >> prop { n: Int =>
      Ok(n).toOption must beSome(n)
    }
  }

  "toList" >> {
    "should return Nil if it's a Fail" >> prop { n: Int =>
      Fail(n).toList must_=== Nil
    }
    "should return List(value) if it's an Ok" >> prop { n: Int =>
      Ok(n).toList must_=== List(n)
    }
  }

  "toTry" >> {
    "should return Failure[B] if it's a Fail[A <:< Exception, B]" >> prop { e: Throwable =>
      Fail(e).toTry must beAFailedTry(e)
    }
    "should return Success[B] if it's n Ok[A <:< Exception, B]" >> prop { n: Int =>
      Ok(n).toTry must beASuccessfulTry(n)
    }
  }

  "ResultFunctions" >> {
    "fail" >> {
      "should create a Fail" >> prop { n: Int =>
        Result.fail(n) must_=== Fail(n)
      }
    }

    "ok" >> {
      "should create an Ok" >> prop { n: Int =>
        Result.ok(n) must_=== Ok(n)
      }
    }

    "fromEither" >> {
      "should put the Left value on the Fail" >> prop { n: Int =>
        Result.fromEither(Left(n)) must_=== Fail(n)
      }
      "should put the Right value on the Ok" >> prop { n: Int =>
        Result.fromEither(Right(n)) must_=== Ok(n)
      }
    }

    "fromOption" >> {
      "should create a Fail if it's None" >> {
        Result.fromOption(None, 1) must_=== Fail(1)
      }
      "should create an Ok if it's Some" >> prop { n: Int =>
        Result.fromOption(Some(n), 1) must_=== Ok(n)
      }
    }

    "fromTry" >> {
      "should create a Fail if it's Failure" >> prop { e: Exception =>
        Result.fromTry(Failure(e)) must_=== Fail(e)
      }
      "should create an Ok if it's Success" >> prop { n: Int =>
        Result.fromTry(Success(n)) must_=== Ok(n)
      }
    }

    "attempt" >> {
      "should catch any NonFatal exception and return it as a Fail" >> prop { e: Exception =>
        def fails() = throw e
        Result.attempt(fails()) must_=== Fail(e)
      }
      "should not catch a Fatal exception" >> {
        def fails() = throw new StackOverflowError
        Result.attempt(fails()) must throwA[StackOverflowError]
      }
      "should wrap the result in an Ok no exception is thrown" >> prop { n: Int =>
        Result.attempt(f(n)) must_=== Ok(f(n))
      }
    }
  }
}