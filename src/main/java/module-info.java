module org.example.telewhat {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires static lombok;
    requires java.validation;
    requires org.hibernate.orm.core;
    requires jbcrypt;
    requires org.postgresql.jdbc;
    requires java.desktop;
    // Hibernate




    opens org.example.telewhat to javafx.fxml;
    opens org.example.telewhat.auth to javafx.fxml;

    // Ouvrir les controllers à FXMLLoader

    // Ouvrir les packages à Hibernate
    opens org.example.telewhat.entity to org.hibernate.orm.core;

    exports org.example.telewhat;
    exports org.example.telewhat.utils;
    opens org.example.telewhat.utils to javafx.fxml;
}