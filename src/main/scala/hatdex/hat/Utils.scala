package hatdex.hat

import scala.collection.immutable.HashMap
import scala.util.{ Failure, Success, Try }

object Utils {
  def flatten[T](xs: Seq[Try[T]]): Try[Seq[T]] = {
    val (ss: Seq[Success[T]] @unchecked, fs: Seq[Failure[T]] @unchecked) =
      xs.partition(_.isSuccess)

    if (fs.isEmpty) Success(ss map (_.get))
    else Failure[Seq[T]](fs(0).exception) // Only keep the first failure
  }

  // Utility function to return None for empty sequences
  def seqOption[T](seq: Seq[T]): Option[Seq[T]] = {
    if (seq.isEmpty)
      None
    else
      Some(seq)
  }

  def reverseOptionTry[T](a: Option[Try[T]]): Try[Option[T]] = {
    a match {
      case None =>
        Success(None)
      case Some(Success(b)) =>
        Success(Some(b))
      case Some(Failure(e)) =>
        Failure(e)
    }
  }

  def mergeMap[A, B](ms: Iterable[HashMap[A, B]])(f: (B, B) => B): HashMap[A, B] =
    (HashMap[A, B]() /: (for (m <- ms; kv <- m) yield kv)) { (a, kv) =>
      a + (if (a.contains(kv._1)) kv._1 -> f(a(kv._1), kv._2) else kv)
    }
}
