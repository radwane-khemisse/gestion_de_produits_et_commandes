## developement du microservice produit

Ce microservice gère le catalogue des produits (CRUD), expose une API REST documentee par Swagger et persiste les donnees dans PostgreSQL via Spring Boot.

Images (PostgreSQL et Swagger):

Capture de la table produits dans PostgreSQL.
![PostgreSQL - table produits](images/postgress_produit-db.png)

Capture de l'interface Swagger affichant toutes les methodes du controleur produit.
![Swagger - product controller](images/product-controller.png)

Capture d'un test d'ajout de produit via Swagger.
![Swagger - tests API](images/add_product_test.png)

Capture du test de recuperation des produits.
![Swagger - get produits](images/test-get-products-shows-product-added.png)
