package langchainkt.data.document.splitter

import langchainkt.data.document.DocumentSplitter
import langchainkt.model.Tokenizer

object DocumentSplitters {
  /**
   * This is a recommended [DocumentSplitter] for generic text.
   * It tries to split the document into paragraphs first and fits
   * as many paragraphs into a single [langchainkt.data.segment.TextSegment] as possible.
   * If some paragraphs are too long, they are recursively split into lines, then sentences,
   * then words, and then characters until they fit into a segment.
   *
   * @param maxSegmentSizeInTokens The maximum size of the segment, defined in tokens.
   * @param maxOverlapSizeInTokens The maximum size of the overlap, defined in tokens.
   * Only full sentences are considered for the overlap.
   * @param tokenizer              The tokenizer that is used to count tokens in the text.
   * @return recursive document splitter
   */
  fun recursive(maxSegmentSizeInTokens: Int,
                maxOverlapSizeInTokens: Int,
                tokenizer: Tokenizer?): DocumentSplitter {
    return DocumentByParagraphSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer,
      DocumentByLineSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer,
        DocumentBySentenceSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer,
          DocumentByWordSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer)
        )
      )
    )
  }

  /**
   * This is a recommended [DocumentSplitter] for generic text.
   * It tries to split the document into paragraphs first and fits
   * as many paragraphs into a single [langchainkt.data.segment.TextSegment] as possible.
   * If some paragraphs are too long, they are recursively split into lines, then sentences,
   * then words, and then characters until they fit into a segment.
   *
   * @param maxSegmentSizeInChars  The maximum size of the segment, defined in characters.
   * @param maxOverlapSizeInTokens The maximum size of the overlap, defined in characters.
   * Only full sentences are considered for the overlap.
   * @return recursive document splitter
   */
  fun recursive(maxSegmentSizeInChars: Int, maxOverlapSizeInTokens: Int): DocumentSplitter {
    return recursive(maxSegmentSizeInChars, maxOverlapSizeInTokens, null)
  }
}
