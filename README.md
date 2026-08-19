# foodics-challenge


### Technology Stack
- [x] Java 21
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


### Configuration

The application is configured entirely through environment variables — there is no per-environment
YAML file to copy. `src/main/resources/application.yaml` is the only Spring config file, and reads
every environment-specific value from a variable.

| Variable | Required | Default | Purpose |
| --- | --- | --- | --- |
| `DB_DRIVER_CLASS_NAME` | No | `org.h2.Driver` | JDBC driver class |
| `DB_URL` | No | `jdbc:h2:mem:testdb` | JDBC connection URL |
| `DB_USERNAME` | **Yes** | — | Database username |
| `DB_PASSWORD` | **Yes** | — | Database password |
| `MAIL_HOST` | No | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | No | `587` | SMTP port |
| `MAIL_USERNAME` | **Yes** | — | SMTP username (a Gmail address, for Google SMTP) |
| `MAIL_PASSWORD` | **Yes** | — | SMTP password (a Google app password, not the account password) |

The datasource and mail credentials never have a built-in default — the application refuses to start
without them, so a real credential can never be silently left in place. The datasource URL/driver
default to an in-memory H2 database, so running without setting anything beyond the four required
credentials still boots — pointing at Postgres for real use just means setting `DB_URL` and
`DB_DRIVER_CLASS_NAME=org.postgresql.Driver`.

`mvn test` needs none of this set — `pom.xml` supplies its own H2 and dummy mail credentials to the
test JVM via the Surefire plugin's `environmentVariables` configuration.

You can set these variables however suits your workflow:

- **Shell export**, before running the app:
  ```bash
  export DB_USERNAME=foodics
  export DB_PASSWORD=foodics
  export MAIL_USERNAME=you@gmail.com
  export MAIL_PASSWORD=your-google-app-password
  ```
- **A `.env` file** loaded by your shell (e.g. via `direnv` or `set -a; source .env; set +a`) — not
  committed, same variable names as above.
- **An IDE run configuration** — add the same variables under the run configuration's environment
  variables, rather than program arguments or `-D` system properties.

### How to run

- Make sure you have both `Java 21` and `Postgres` installed
- Set the environment variables described above
- Run using the command `mvn spring-boot:run`
