package hatdex.hat

import scala.util.{Failure, Success, Try}

object Utils {
  def flatten[T](xs: Seq[Try[T]]): Try[Seq[T]] = {
    val (ss: Seq[Success[T]]@unchecked, fs: Seq[Failure[T]]@unchecked) =
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
}
