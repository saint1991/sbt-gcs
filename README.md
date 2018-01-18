# sbt-gcs

[![Build Status](https://travis-ci.org/saint1991/sbt-gcs.svg?branch=master)](https://travis-ci.org/saint1991/sbt-gcs)
[![Download](https://api.bintray.com/packages/saint1991/sbt-plugins/sbt-gcs/images/download.svg)](https://bintray.com/saint1991/sbt-plugins/sbt-gcs/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

sbt-gcs is a simple sbt plugin to manipulate objects on Google Cloud Storage.


## Install

As a prerequisite, sbt 0.13.16 or newer is required.  
In order to add the sbt-gcs plugin to your build, add the following to `project/plugins.sbt`.


```project/plugins.sbt
resolvers += Resolver.bintrayIvyRepo("saint1991", "sbt-plugins")
addSbtPlugin("com.github.saint1991" % "sbt-gcs" % "0.1.0")
```

This plugin automatically add some tasks to manipulate Google Cloud Storage.


## Usage

### Uploading

To upload files to Google Cloud Storage, you need to specify source/destination mapping at the `mappings` key.

```build.sbt

// upload a text file and build artifact created by sbt-assembly
mappings in gcsUpload := Seq(
  Path.userHome / "foo.txt" -> "gs://bucket_name/path/to/upload/foo.txt",
  assembly.value -> "gs://bucket_name/path/to/artifact.jar" 
)
```

Then execute it as follows:
```bash
$ sbt gcsUpload
```


### Downloading

Similarly, to download objects from Google Cloud Storage, you also need to specify mappings.
Please note that you need to write it in the destination -> source order.
For example, by the following settings, `image.png` will be downloaded from 
`gs://image_bucket/path/to/image.png` to the user home.

```build.sbt

// download an image file from  
mappings in gcsUpload := Seq(
  (Path.userHome / "image.png" , "gs://image_bucket/path/to/image.png") 
)
```

Executing it by:

```bash
$ sbt gcsDownload
```


### Deleting

To delete objects from Google Cloud Storage, you need to specify a list of 
object URLs to delete via the `gcsUrls` key.

```build.sbt
gcsUrls in gcsDelete := Seq(
  "gs://bucket_name/path/to/object/to/delete.json"
)
``` 

Executing it by:
```
$ sbt gcsDelete
```

## Credentials
By default, sbt-gcs uses default credentials stated [here](https://cloud.google.com/video-intelligence/docs/common/auth).  You can also explicitly set it via `gcsCredential` key.

e.g.
```
import java.io.FileInputStream
import com.google.auth.oauth2.UserCredentials

gcsCredential := Some(
    UserCredentials.fromStream(new FileInputStream(new File("credential.json")))
)
```


## License
This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)