        CREATE TABLE user (
            id INT NOT NULL AUTO_INCREMENT,
            name VARCHAR(255) NOT NULL,
            surname VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL UNIQUE,
            username VARCHAR(255) NOT NULL,
            tax_code VARCHAR(255) NOT NULL,
            PRIMARY KEY (id)
       );

       CREATE TABLE roles_user (
           id INT NOT NULL AUTO_INCREMENT,
           role VARCHAR(255) NOT NULL,
           user_id INT NOT NULL,
           FOREIGN KEY (user_id) REFERENCES user(id),
           PRIMARY KEY (id),
           UNIQUE (user_id, role)
      );