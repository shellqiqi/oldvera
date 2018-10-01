package org.change.v2.runners.experiments.routerexperiments

object FibDiff {

  /**
    * Takes two parsed fibs, sorts them by mask length and (if the case) by first address.
    *
    * Compares for equality the two.
    * @param fibA
    * @param fibB
    * @return
    */
  def compare(fibA: Seq[((Long, Long), String)], fibB: Seq[((Long, Long), String)]): Boolean = {
    def comparator: (((Long, Long), String), ((Long, Long), String)) => Boolean = (a, b) => {
      var r = (a._1._2 - a._1._1).compareTo(b._1._2 - b._1._1)

      if (r == 0)
        r = a._1._1.compareTo(b._1._1)

      if (r < 0)
        true
      else
        false

    }

    (fibA.sortWith( comparator ) zip fibB.sortWith( comparator )).forall(e => e._1 == e._2)
  }

}
