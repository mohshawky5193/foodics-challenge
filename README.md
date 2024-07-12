# foodics-challenge


### Technology Stack
- [x] Java 17
- [x] Spring Boot
- [x] Postgresql
- [x] Google SMTP

### Architecture

The architecture is layered architecture consisting of `controller` which is the `OrderController` <br/>
and then the `service` layer containing of many services like `ProductService` , `IngredientService` and `OrderService` and `EmailService`<br/>
and the last layer is the `repository` layer which is responsible for saving the data in the database. the `EmailService` uses Google SMTP to send emails


### Assumptions
- The `Product` and `Ingredient` are prefilled by DBA so it is out of scope of the application and that's why we have `DatabaseInitializer` which simulated the operation of filling the products and their ingredients along with their amount

- Any order which will make any ingredient go out of stock will be rejected
 
