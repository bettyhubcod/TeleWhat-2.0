module org.example.telewhat {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires static lombok;
    requires java.validation;
    requires org.hibernate.orm.core; // Hibernate
    requires jbcrypt;

    // Ouvrir les controllers à FXMLLoader
    opens org.example.telewhat.auth to javafx.fxml;

    // Ouvrir les packages à Hibernate
    opens org.example.telewhat.entity to org.hibernate.orm.core;

    // Exports pour les autres modules si nécessaire
    exports org.example.telewhat;
}