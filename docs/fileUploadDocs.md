# HAT File Storage

File storage is a rather different beast from structured (meta)data about your personal digital life:

- they are big to store
- sending them back and forth requires a lot of bandwidth
- databases very useful for storing structured data are not well-suited for storing files
- data in a file can not normally be sliced and diced as structured metadata could
- files would often be uploaded from a low-bandwidth (e.g. mobile) device that is likely to have intermittent connectivity

These considerations further play with the requirements for file storage to be reliable, secure and cost-efficient especially if we want to make HATs affordable. And we still want to be able to attach metadata to files, maintain fine-granularity access control.

APIs are also rather different from web pages in terms of how file uploads tend to be or can be handled. Not going into a lot of detail here there are plenty of reasons why [uploading using multipart forms mostly sucks](https://philsturgeon.uk/api/2016/01/04/http-rest-api-file-uploads/) as well as pretty good examples on how file uploads could be done, e.g. [YouTube Resumable Uploads](https://developers.google.com/youtube/v3/guides/using_resumable_upload_protocol). As a building block to satisfy all the requirements we picked AWS S3

## Uploads

File upload happens in three steps:

1. Posting file metadata to the HAT and retrieving URL to send the file to directly
2. Directly uploading the file to the (securely signed) URL
3. Marking the file complete at the HAT

Uploading metadata is simple: call `POST /api/v2/files/upload` with the file details:

```curl
   curl -X POST -H "Accept: application/json" -H "X-Auth-Token: ${HAT_AUTH_TOKEN}" \
    	-H "Content-Type: application/json" \
    	-d '{
			"name": "testFile.png",
			"source": "test",
			"tags": ["tag1", "tag2"],
			"title": "test Title",
			"description": "a very interesting test file",
		}' \
		"https://${HAT_ADDRESS}/api/v2/files/upload"
```

Only `name` and `source` properties are mandatory - all others are optional. You can also attach `dateCreated` and `lastUpdated` fields with Unix timestamps to set them accordingly. If everything is successful, the HAT will respond with a copy of the metadata as well as additional information:

```
{
  "fileId": "testtestfile-12.png",
  "name": "testFile.png",
  "source": "test",
  "tags": ["tag1", "tag2"],
  "title": "test Title",
  "description": "a very interesting test file",
  "dateCreated": 1487871142325,
  "lastUpdated": 1487871142329,
  "status": {
    "status": "New"
  },
  "contentUrl": "https://hat-storage-test.s3.amazonaws.com/HAT_ADDRESS/testtestfile-12.png?AWSAccessKeyId=AKIAJSOXH3FJPB43SWGQ&Expires=1487871442&Signature=CTRdDW8nKBqNcuwK0ssH77zjkec%3D",
  "contentPublic": false,
  "permissions": [
    {
      "userId": "694dd8ed-56ae-4910-abf1-6ec4887b4c42",
      "contentReadable": true
    }
  ]
}
```

Importantly, it includes the unique file identifier for the HAT `fileId` and `contentUrl` indicating where the file should be uploaded. The upload `contentUrl` is signed and has limited duration validity, most likely 5 minutes, after which it becomes invalid. Then uploading itself could be done as (*note:* `x-amz-server-side-encryption` hearder is mandatory):

```curl
curl -v -T ${LOCAL_FILE} \
  -H "x-amz-server-side-encryption: AES256"\
  "https://hat-storage-test.s3.amazonaws.com/HAT_ADDRESS/testtestfile-12.png?AWSAccessKeyId=AKIAJSOXH3FJPB43SWGQ&Expires=1487871442&Signature=CTRdDW8nKBqNcuwK0ssH77zjkec%3D"
```

Finally, to mark the file "Completed", call `PUT /api/v2/files/file/:fileId/complete`. It will again respond with file metadata:

```
{
  "fileId": "postmantestfile-12.png",
  "name": "testFile.png",
  "source": "postman",
  "tags": ["tag1", "tag2"],
  "title": "test Title",
  "description": "a very interesting test file",
  "dateCreated": 1487871142325,
  "lastUpdated": 1487871142329,
  "status": {
    "size": 154639,
    "status": "Completed"
  },
  "contentPublic": false,
  "permissions": [
    {
      "userId": "694dd8ed-56ae-4910-abf1-6ec4887b4c42",
      "contentReadable": true
    }
  ]
}
```

File `status` has now been marked as `Completed` and also contains file size in bytes! The request will fail if the file doesn't exist, hasn't been fully uploaded or you do not have permissions to mark the file completed (you will if you started the upload in the first place).

Finally, files can be deleted (by *owner* only!) by calling `DELETE /api/v2/files/file/:fileId`

## Viewing contents

- `GET api/v2/files/file/:fileId` to list metadata of a file, including `contentUrl` pointing to a pre-signed temporary URL for file contents if the user is permitted file access
- `GET /api/v2/files/content/:fileId` to get contents of a file if file is marked publicly accessible or the client is permitted file content access. The endpoint redirects to the pre-signed temporary content URL or returns 404 error code (Not Found) if the file does not exist or is not accessible

## Access Control

HAT owner can access all files, but otherwise there are three options for file access:

- another HAT user can be marked to have access to file's metadata
- another HAT user can be marked to have access to both file's metadata and contents
- a file can be marked to have its contents publicly accessible (e.g. publishing photos or your book!)

By default, the user who saved the file onto the HAT is allowed to see the file's metadata and contents, but only the `owner` can adjust file permissions by:

- calling `GET /api/v2/files/allowAccess/:fileId/:userId` to allow a specific user (`:userId`) to access a specific file (`:fileId`), optionally setting `content` query parameter to `true`/`false` to control content access (`false` by default). Conversely, calling `GET /api/v2/files/restrictAccess/:fileId/:userId` to restrict access.
- calling `POST /api/v2/files/allowAccess/:userId` sending file template to grant access to a set of files (same syntax as for file search!). Conversely, calling `POST /api/v2/files/restrictAccess/:userId` to restrict access.
- calling `GET /api/v2/files/allowAccessPublic/:fileId` and `GET /api/v2/files/restrictAccessPublic/:fileId` to control public file access

## Search

HAT files can be looked up by any part of metadata attached to them:

- `fileId` for an exact match, where one or no files are returned
- `fileName` for an exact match on the original name, but multiple files could potentially be returned. *Empty* string if you do not want to match against `fileName`
- `source` matching all files from a specific source such as `facebook`. *Empty* string if you do not want to match against `source`
- `tags` a set of all tags matching files need to have attached
- `title` and `description` for an approximate, text-based search matching the fields
- `status` to filter e.g. only files that are marked `Completed`

To search for files call `POST /api/v2/files/search` sending file template to match against. All calls must be authenticated with the user's token and only files the user is allowed to access are returned (all files for the HAT owner!):

```curl
    curl -X POST -H "Accept: application/json" -H "X-Auth-Token: ${HAT_AUTH_TOKEN}" \
    	-H "Content-Type: application/json" \
    	-d '{
			"name": "testFile.png",
			"source": "test",
			"tags": ["tag1", "tag2"],
			"title": "test Title",
			"description": "a very interesting test file",
			"status": { "status": "Completed", "size": 0}
		}' \
		"https://${HAT_ADDRESS}/api/v2/files/search"
```
