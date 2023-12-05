package flatNetworkFileSystem;

import java.io.File;
import java.io.Serializable;

public class Response implements Serializable {
    private String message;
    private String error;
    private long value;

        public Response(String message, String error, long fileSize) {
        this.message = message;
        this.error = error;
        this.value = fileSize;
    }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getValue() {return value;}

        public void setError(String error) {this.error = error;}

        public void setValue(long value) {this.value = value;}

        public String getError() {return error;}
    }
