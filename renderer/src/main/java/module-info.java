module com.summertech20206 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.logging;
    requires java.compiler;
    requires org.fxyz3d.core;
    requires JavaCad;
    requires jcsg;
    
    opens com.summertech20206 to javafx.fxml;
    exports com.summertech20206;
}
