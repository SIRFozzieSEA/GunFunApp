# GunFunApp

### Purpose

The purpose of the application was a way for me to keep track of my guns.  The app can track basic information about each gun, how many bullets  fired through it, when it was last cleaned, when it was last carried, etc.  You can log your daily carry, cleanings, shots, and provide basic reporting on those metrics. There is also a basic quiz feature built in as well... in order to use this feature, you would need to click on `Rebuild Questions` on the main navigation then you should be able to click on `Quiz` to launch a quiz session.

### Requirements

This was designed to work with Java 17.x, your mileage may vary using anything other than that.  The application is backed with an embedded H2 database which gets stored locally on your PC.

You will also need to make sure you have Lombok (https://projectlombok.org/) loaded since I am a slob and don't want to generate stubs for my POJO and entities.

### Getting Started

To get the project running for the first time (build tables, app folders, sample images/pdfs, etc.) do the following:

- Open the `application.properties` file in the IDE.
- Edit the key `GUNFUN_APP_FOLDER` and set it to the path you'd like your application assets (data, images, pdfs, backups) to go into.
- Edit the key `server.port` and set it to whatever port you'd like the service to run on.
- Edit the key `spring.datasource.password` and set it to whatever password you want to secure your database with.
- Edit the key `spring.jpa.hibernate.ddl-auto` and set it to `create`. 
- Save the `application.properties` file.
- Launch the `com.codef.gunfunapp.GunFunApplication` class from the IDE.
- Let the application load completely, and keep the application **running** for all steps below this.

- Edit the key `spring.jpa.hibernate.ddl-auto` value to `none`.
- Edit the key `spring.datasource.initialization-mode` value to `always`.
- Save the file.  If the application is still running, the devtools should reload the context automatically.

- Database should be rebuilt now and will show up in the path. Next we'll revert to default values for normal operation.
- Edit the key `spring.datasource.initialization-mode` value to `never`.
- Save the file.  If the application is still running, the devtools should reload the context automatically.

- Click to launch the app.  Default URL is `http://localhost:9002/`
- In the left navigation bar, click `Maintenance Functions` then `Build Sample Assets`.  This feature will disappear from the maintenance screen after the first run.  If you wish to re-run to overwrite the assets. Go into `Maintenance Functions`, `Manage Preferences` and then change the value for `SAMPLE_ASSETS_BUILT` to `true`, then click `Submit`.  Once you go back into the maintenance screen, the option should re-appear.
- In the left navigation bar, click `Maintenance Functions` then `Rebuild Quiz Questions`.
- That's it!  Have fun!

### Release process

Run `mvn install` to create a new jar file and then when running outside of Eclipse (or other IDE), use the `java -jar gunfunapp-0.0.1-SNAPSHOT.jar` to execute.  As I run this at startup, I have a batch file which launches it from the `target` folder. This will also keep from making mistakes like running your local target folder at the same time as working on code.


### Other Notes

- **Backups** -- The backup feature allows you to backup when the application is running.  Data will be exported as SQL dump, tab delimited files, images will be backed up in a .zip file, manuals will be backed up in a .zip file as well.  Backing up the properties file will result in a single file.  All backups are prefixed by the date, and will be written into the `[GUNFUN_APP_FOLDER]\_backup` folder.  Also included in the `[GUNFUN_APP_FOLDER]\_data` folder is a .bat script that will copy the raw H2 data file... the database should **not be running** when this script is executed to make sure all data is copied.
- **Carry Logs** -- In interest to keeping the carry logs up to date, you can create a desktop shortcut with `http://localhost:9002/log/carry_add?SHUTDOWN_AFTER=true` which will launch the carry log screen, update your entries and shut down the computer.
