package forcomp


object Anagrams {

  /** A word is simply a `String`. */
  type Word = String

  /** A sentence is a `List` of words. */
  type Sentence = List[Word]

  /** `Occurrences` is a `List` of pairs of characters and positive integers saying
   *  how often the character appears.
   *  This list is sorted alphabetically w.r.t. to the character in each pair.
   *  All characters in the occurrence list are lowercase.
   *
   *  Any list of pairs of lowercase characters and their frequency which is not sorted
   *  is **not** an occurrence list.
   *
   *  Note: If the frequency of some character is zero, then that character should not be
   *  in the list.
   */
  type Occurrences = List[(Char, Int)]

  /** The dictionary is simply a sequence of words.
   *  It is predefined and obtained as a sequence using the utility method `loadDictionary`.
   */
  val dictionary: List[Word] = loadDictionary

  /** Converts the word into its character occurrence list.
   *
   *  Note: the uppercase and lowercase version of the character are treated as the
   *  same character, and are represented as a lowercase character in the occurrence list.
   *
   *  Note: you must use `groupBy` to implement this method!
   */

  def wordOccurrences(w: Word): Occurrences = {
    val lowCaseWord = w.toLowerCase();
    lowCaseWord.groupBy((ch:Char) => ch).map({case (k,v) => (k,v.size)}).toList.sortWith(_._1 < _._1)
  }

  def sortSentenceOccurrences(o:List[Char])(o1:(Char,Int), o2:(Char,Int)): Boolean ={
    o.indexOf(o1._1) < o.indexOf(o2._1)
  }

  def mergeOccurrences(occurrences: List[Occurrences]):Occurrences = {
    val dataToMerge = occurrences.reduceLeft( _ ::: _)
    val orderedChars = dataToMerge.map(_._1)
    val mergedData = dataToMerge.groupBy((a:(Char,Int)) => a._1).map{
      case (k,v) => v match{
        case l:List[(Char,Int)] => (k,l.map(_._2).sum)
      }
    }
    mergedData.toList.sortWith(sortSentenceOccurrences(orderedChars))
  }

  /** Converts a sentence into its character occurrence list. */
  def sentenceOccurrences(s: Sentence): Occurrences = {
    val occurrences = s.map(wordOccurrences)
    occurrences match {
      case Nil => Nil
      case _ => mergeOccurrences(occurrences)
    }
  }

  /** The `dictionaryByOccurrences` is a `Map` from different occurrences to a sequence of all
   *  the words that have that occurrence count.
   *  This map serves as an easy way to obtain all the anagrams of a word given its occurrence list.
   *
   *  For example, the word "eat" has the following character occurrence list:
   *
   *     `List(('a', 1), ('e', 1), ('t', 1))`
   *
   *  Incidentally, so do the words "ate" and "tea".
   *
   *  This means that the `dictionaryByOccurrences` map will contain an entry:
   *
   *    List(('a', 1), ('e', 1), ('t', 1)) -> Seq("ate", "eat", "tea")
   *
   */

  lazy val dictionaryByOccurrences: Map[Occurrences, List[Word]] = {
     dictionary.map((word:Word) => (word,wordOccurrences(word))).groupBy(_._2).map{
      case (o,v) => v match {
        case m:List[(Word,Occurrences)] => (o,m.map(_._1))
      }
    }
  }

  /** Returns all the anagrams of a given word. */
  def wordAnagrams(word: Word): List[Word] = {
    dictionaryByOccurrences get( wordOccurrences(word)) match {
      case Some(words) => words
      case _ => List()
    }
  }

  def charCombination(occurrences: Occurrences):List[Occurrences] = {
    occurrences.toSet.subsets.map(_.toList).toList
  }


  def countDownOccurrences(occurrences: Occurrences):List[Occurrences] = {
    occurrences match {
      case (char:Char,count:Int) :: tail =>
        for{
          others <- countDownOccurrences(tail)
          x <- 1 to count}
          yield (char,x) :: others
      case  Nil =>
        List(List())
    }
  }

  /** Returns the list of all subsets of the occurrence list.
   *  This includes the occurrence itself, i.e. `List(('k', 1), ('o', 1))`
   *  is a subset of `List(('k', 1), ('o', 1))`.
   *  It also include the empty subset `List()`.
   *
   *  Example: the subsets of the occurrence list `List(('a', 2), ('b', 2))` are:
   *
   *    List(
   *      List(),
   *      List(('a', 1)),
   *      List(('a', 2)),
   *      List(('b', 1)),
   *      List(('a', 1), ('b', 1)),
   *      List(('a', 2), ('b', 1)),
   *      List(('b', 2)),
   *      List(('a', 1), ('b', 2)),
   *      List(('a', 2), ('b', 2))
   *    )
   *
   *  Note that the order of the occurrence list subsets does not matter -- the subsets
   *  in the example above could have been displayed in some other order.
   */

  def combinations(occurrences: Occurrences): List[Occurrences] = {
    charCombination(occurrences).map(countDownOccurrences).flatten.toSet.toList
  }

  /** Subtracts occurrence list `y` from occurrence list `x`.
   *
   *  The precondition is that the occurrence list `y` is a subset of
   *  the occurrence list `x` -- any character appearing in `y` must
   *  appear in `x`, and its frequency in `y` must be smaller or equal
   *  than its frequency in `x`.
   *
   *  Note: the resulting value is an occurrence - meaning it is sorted
   *  and has no zero-entries.
   */
  def subtract(x: Occurrences, y: Occurrences): Occurrences = {
    val data = scala.collection.mutable.HashMap.empty[Char,Int];

    for((ch,count) <- x){
      data.put(ch,count)
    }
    for((ch,count)<- y){
      data.get(ch) match {
        case Some(result) => data.put(ch,result-count);
        case _ =>
      }
    }
    data.filter(_._2 >0).toList.sortWith(sortSentenceOccurrences(x.map(_._1)))
  }

  def isSubset(x:Occurrences,y:Occurrences):Boolean = {
    val data = scala.collection.mutable.HashMap.empty[Char,Int];

    for((ch,count) <- x){
      data.put(ch,count)
    }
    for((ch,count)<- y){
      data.get(ch) match {
        case Some(result) => data.put(ch,result-count);
        case None => data.put(ch,-count)
      }
    }
    data.filter(_._2 <0).isEmpty
  }

  def getWords(occurrences: Occurrences):List[Word] = {
    dictionaryByOccurrences.get(occurrences.sortWith(_._1 < _._1)) match {
      case Some(x) => x
      case None => Nil
    }
  }

  def combineSentence(words:List[List[Word]]):List[Sentence] = {
    words match {
      case x::tail => for(w <- x;sentence <- combineSentence(tail)) yield w::sentence
      case Nil => List(List())
    }
  }

  def getSentences(occurrences: List[Occurrences]):List[Sentence] = {
    val allSolutions = occurrences.map(getWords(_))
    combineSentence(allSolutions)
  }

  def getAllPossibleWords(occurrences: Occurrences):Map[Word,Occurrences] = {
    if(occurrences.isEmpty){
      Map[Word,Occurrences]()
    }
    val data =
      for{subOccurrency <- combinations(occurrences)
          word <- getWords(subOccurrency).filter(_!=Nil)
      }yield  (word,subOccurrency)
    data toMap
  }

  def occurrencesMatch(totalOcc:Occurrences)(occ: Occurrences,occurrences: List[Occurrences]):Boolean = {
    val mergedOne = mergeOccurrences(occ::occurrences)
    isSubset(mergedOne,totalOcc) && isSubset(totalOcc,mergedOne)
  }

  def getLeftWords(wordsDictionary:List[Occurrences])(leftOcc:Occurrences):List[List[Occurrences]] = {
    if(leftOcc.isEmpty) return List(List())
    else {
      for{
        occ <- wordsDictionary.filter{case x:Occurrences => isSubset(leftOcc,x)}
        leftOccurrences <- getLeftWords(wordsDictionary)(subtract(leftOcc,occ))
        if occurrencesMatch(leftOcc)(occ,leftOccurrences)
      }yield occ :: leftOccurrences
    }
  }


  /** Returns a list of all anagram sentences of the given sentence.
   *
   *  An anagram of a sentence is formed by taking the occurrences of all the characters of
   *  all the words in the sentence, and producing all possible combinations of words with those characters,
   *  such that the words have to be from the dictionary.
   *
   *  The number of words in the sentence and its anagrams does not have to correspond.
   *  For example, the sentence `List("I", "love", "you")` is an anagram of the sentence `List("You", "olive")`.
   *
   *  Also, two sentences with the same words but in a different order are considered two different anagrams.
   *  For example, sentences `List("You", "olive")` and `List("olive", "you")` are different anagrams of
   *  `List("I", "love", "you")`.
   *
   *  Here is a full example of a sentence `List("Yes", "man")` and its anagrams for our dictionary:
   *
   *    List(
   *      List(en, as, my),
   *      List(en, my, as),
   *      List(man, yes),
   *      List(men, say),
   *      List(as, en, my),
   *      List(as, my, en),
   *      List(sane, my),
   *      List(Sean, my),
   *      List(my, en, as),
   *      List(my, as, en),
   *      List(my, sane),
   *      List(my, Sean),
   *      List(say, men),
   *      List(yes, man)
   *    )
   *
   *  The different sentences do not have to be output in the order shown above - any order is fine as long as
   *  all the anagrams are there. Every returned word has to exist in the dictionary.
   *
   *  Note: in case that the words of the sentence are in the dictionary, then the sentence is the anagram of itself,
   *  so it has to be returned in this list.
   *
   *  Note: There is only one anagram of an empty sentence.
   */
  def sentenceAnagrams(sentence: Sentence): List[Sentence] = {
    val sentenceOccurrencies = sentenceOccurrences(sentence)
    sentenceOccurrencies match{
      case Nil => List(Nil)
      case _ => {
        val allPossibleWords = getAllPossibleWords(sentenceOccurrencies)
        val allPossibleOccurrences = allPossibleWords.groupBy(_._2).map{
          case (o,pp) => pp match {
            case mp:Map[Word,Occurrences] => (o,mp.keys.toList)
          }
        }
        val allPossibleSolutions = getLeftWords(allPossibleOccurrences.map(_._1).toList)(sentenceOccurrencies)
        allPossibleSolutions.map(getSentences).flatten
      }
    }
  }
}