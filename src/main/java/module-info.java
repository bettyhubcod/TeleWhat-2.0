module org.example.telewhat {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires static lombok;
    requires java.validation;
    requires org.hibernate.orm.core; // Hibernate


    opens org.example.telewhat to javafx.fxml;


    opens org.example.telewhat.entity to org.hibernate.orm.core;

    exports org.example.telewhat;
}