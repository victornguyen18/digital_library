# Digital Library IU

The Digital Library IU is a digital library website and it is my thesis project.
The website is a example of how recommendation systems work, and how you can implement them.

Moreover, I reuse java application to apply ontology search engine and web portal
(Get result from Google Books). It help user get more result from different sites.
Moreover, user can find relevant books by ontology search engine.

## Required services/applications:
* Python 3: [Download](https://www.python.org/Downloads/) 
* Java: [Download](https://www.java.com/en/Download/)
    * Install Java JDK [Download](https://www.oracle.com/technetwork/java/javase/Downloads/jdk8-Downloads-2133151.html)
* Install Gradle [Download](https://gradle.org/releases/).
Look at the following guide for more details 
    [guide](https://youtu.be/_XcO_BujfeQ).
    * Step 1. Download the latest Gradle distribution
    * Step 2. Unpack the distribution
    * Step 3. Configure your system environment
    * Step 4. Verify your installation
    
## Project Setup
The following is expecting you to have python 3.x installed on your machine. I recommend
 looking that the [Hitchhikers guide to Python](http://docs.python-guide.org/en/latest/) if you 
 haven't.
 
 For windows users it's a good idea to install the Anaconda package. Anaconda is the leading open 
 data science platform powered by Python (according to their homepage) [Anaconda](https://www.continuum.io/Downloads)
 
### [OPTIONAL]Create a virtual environment for the project 

Look at the following guide for more details [guide](http://docs.python-guide.org/en/latest/dev/virtualenvs/#virtualenvironments-ref)

Run terminal/command line on project folder
* Windows
```bash
# cd digital_library
> pip install virtualenvwrapper-win
> mkvirtualenv env_library
> setprojectdir .
```

* Linux/MacOS
```bash
# cd digital_library
> pip install virtualenv
> virtualenv venv
> source venv/bin/activate
```

if you are running Anaconda you can also use conda virtual environment instead.

### Get the required packages

```bash
pip3 install -r requirements.txt
```
## Database setup

#### Configuration

To update the database in DIGITAL LIBRARY go to in book_management/settings.py 
and update the following 

```bash
DATABASES = {
   'default': {
       'ENGINE': 'django.db.backends.sqlite3',
       'NAME': os.path.join(BASE_DIR, 'db.sqlite3'),
   }
}
```

#### Create the dbs. 
If you have a database running on your machine I would encourage 
you to connect it, by updating the settings in `book_management/settings.py` (fx like shown above). 

To set up another database is described in the Django docs [here](https://docs.djangoproject.com/en/2.0/ref/databases/)
```bash
> python3 manage.py makemigrations
> python3 manage.py migrate
```
#### Initial db by running the following script. 
[WARNING][This step will take time because of training data]
```bash
> python3 import_data.py
> python3 train.py
```

## Start the web server
 To run web portal and ontology search engine.
 You mush build and run java project first.
 * In Windows
 ```bash
> cd digital_library_java
> gradle build
> gradle run
```

* In Linux/Mac
 ```bash
> cd digital_library_java
> ./gradlew build
> ./gradlew run
```

 * Open another terminal/command line

 To start the development server run:
```bash
> python3 manage.py runserver 127.0.0.1:8000
```
Running the server like this, will make the website available 
[http://127.0.0.1:8000](http://127.0.0.1:8000) other applications also use this port
so you might need to try out 8001 instead. 
* Account admin to login admin
    * Username: admin
    * Password: adminadmin
    * Admin Page of Django build:[http://127.0.0.1:8000/admin2](http://127.0.0.1:8000/admin2)
    * Admin Page of System:[http://127.0.0.1:8000/admin](http://127.0.0.1:8000/admin)
* Account of other user:
    * Username: in dbs ititiu15050
    * Password: Default123456

### Closing down.
when you are finished running the project you can:
* Close down the server by pressing <CLTR>-c  
* exit the virtual env:
```bash
> deactivate
```

> Created by
>
> Nguyen Pham Xuan Thang