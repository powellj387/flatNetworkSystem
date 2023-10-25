package flatNetworkFileSystem;

import java.io.Serializable;

public class Response implements Serializable {
    private String message;
    private String error;
    private byte[] value;

        public Response(String message, String error, byte[] value) {
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

        public byte[] getValue() {return value;}

        public void setError(String error) {this.error = error;}

        public void setValue(byte[] value) {this.value = value;}

        public String getError() {return error;}
    }
