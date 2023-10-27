//@author Julian Powell

package flatNetworkFileSystem;
import java.io.File;
import java.io.Serializable;

public class Request implements Serializable {
    private String method;
    private String fileName;
    private File fileData;

    public Request(String method, String fileName, File fileData) {
        this.method = method;
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public String getMethod() {
        return method;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFileData() {
        return fileData;
    }
}
