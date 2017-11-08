
HAT Data Feed structure

- source: string
- date: UNIX timestamp
- types: Array of strings, e.g. (photo, post, event, link, video, place) 
- title: Object (optional)
    - text: String
    - action: String name of an icon?
- content: object
    - text: String
    - media: Object array
        -
          url: String
- location: optional object
	- geo: optional object (longitude, latitude, etc)
	- address: optional object (normal address with street, city, country, etc)
	- tags: Optional array of strings (home, work, etc)

```json
[
{
    "source": "fitbit",
    "date": 1509312799,
    "title":
    {
        "text": "You walked 3789 steps",
        "action": "steps"
    },
    "content": null,
    "location": null
},
{
    "source": "facebook",
    "date": 1509265200,
    "title":
    {
        "text": "You posted this photo",
        "action": null
    },
    "content":
    {
        "text": null,
        "media": [
        {
            "url": "https://camo.githubusercontent.com/f92f247a903901becb770b14354654061fdaaa4b/687474703a2f2f696d672e796f75747562652e636f6d2f76692f79317478596a6f535151632f302e6a7067"
        }]
    }
},
{
    "source": "twitter",
    "date": 1509264200,
    "title":
    {
        "text": "You retweeted",
        "action": null
    },
    "content":
    {
        "text": "Son: Dad can we go to a haunted house this year?\nMe: What's wrong with the one we live in?\nSon: What?!?\nMe: Goodnight son."
    }
},
{
    "source": "instagram",
    "date": 1509255200,
    "title":
    {
        "text": "You posted this photo",
        "action": null
    },
    "content":
    {
        "text": "Commuting to my new desk #worklifebalance",
        "media": [
        {
            "url": "https://media.wired.com/photos/59327d844dc9b45ccec5e7ca/master/pass/bikedesk.jpg"
        }]
    }
},
{
    "source": "hat",
    "date": 1509245200,
    "title":
    {
        "text": "You got yourself a HAT",
        "action": null
    },
    "content":
    {
        "text": "Together we can change the Internet."
    }
}
]
```
