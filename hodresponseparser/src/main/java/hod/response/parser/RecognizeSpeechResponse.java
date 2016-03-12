package hod.response.parser;

import java.util.List;

/**
 * Created by vuv on 9/24/2015.
 */
public class RecognizeSpeechResponse {
    public List<Document> document; // (array[Document]) The speech block transformed to text.
    public class Document {
        public Integer offset; // (integer, optional) The offset of the first word in this content section.
        public String content; // (string) The extracted block of text from speech.
    }
}
