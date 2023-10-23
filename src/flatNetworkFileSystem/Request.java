//@author Julian Powell

package flatNetworkFileSystem;
import java.io.Serializable;

public class Request implements Serializable {
    private String method;
    private String fileName;
    private String localPath;

    public Request(String method, String fileName, String localPath) {
        this.method = method;
        this.fileName = fileName;
        this.localPath = localPath;
    }

    public String getMethod() {
        return method;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLocalPath() {
        return localPath;
    }
}
