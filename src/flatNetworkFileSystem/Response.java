package flatNetworkFileSystem;

import java.io.File;
import java.io.Serializable;

public class Response implements Serializable {
    private String message;
    private String error;
    private File value;

        public Response(String message, String error, File value) {
        this.message = message;
        this.error = error;
        this.value = value;
    }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public File getValue() {return value;}

        public void setError(String error) {this.error = error;}

        public void setValue(File value) {this.value = value;}

        public String getError() {return error;}
    }
