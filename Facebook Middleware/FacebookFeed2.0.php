<?php
ini_set('display_errors', 1);
 
// Facebook PHP SDK v4.0.8
 
// path of these files have changes
require_once( 'Facebook/HttpClients/FacebookHttpable.php' );
require_once( 'Facebook/HttpClients/FacebookCurl.php' );
require_once( 'Facebook/HttpClients/FacebookCurlHttpClient.php' );
 
require_once( 'Facebook/Entities/AccessToken.php' );
require_once( 'Facebook/Entities/SignedRequest.php' );
 
// other files remain the same
require_once( 'Facebook/FacebookSession.php' );
require_once( 'Facebook/FacebookRedirectLoginHelper.php' );
require_once( 'Facebook/FacebookRequest.php' );
require_once( 'Facebook/FacebookResponse.php' );
require_once( 'Facebook/FacebookSDKException.php' );
require_once( 'Facebook/FacebookRequestException.php' );
require_once( 'Facebook/FacebookOtherException.php' );
require_once( 'Facebook/FacebookAuthorizationException.php' );
require_once( 'Facebook/GraphObject.php' );
require_once( 'Facebook/GraphSessionInfo.php' );
require_once('Facebook/GraphUser.php');
 
// path of these files have changes
use Facebook\HttpClients\FacebookHttpable;
use Facebook\HttpClients\FacebookCurl;
use Facebook\HttpClients\FacebookCurlHttpClient;
 
use FacebookEntities\AccessToken;
use FacebookEntities\SignedRequest;
 
// other files remain the same
use Facebook\FacebookSession;
use Facebook\FacebookRedirectLoginHelper;
use Facebook\FacebookRequest;
use Facebook\FacebookResponse;
use Facebook\FacebookSDKException;
use Facebook\FacebookRequestException;
use Facebook\FacebookOtherException;
use Facebook\FacebookAuthorizationException;
use Facebook\GraphObject;
use Facebook\GraphSessionInfo;
use Facebook\GraphUser;
 
// start session
session_start();
 
// init app with app id and secret
FacebookSession::setDefaultApplication( '1483942438522701','d188ca5467347a410eb967d6bff396d6' );
 
// login helper with redirect_uri
$helper = new FacebookRedirectLoginHelper( 'http://hubofallthings.com/HAT-Apps/src/FacebookFeedNoPost.php' );
 
// see if a existing session exists
if ( isset( $_SESSION ) && isset( $_SESSION['fb_token'] ) ) {
  // create new session from saved access_token
  $session = new FacebookSession( $_SESSION['fb_token'] );
  
  // validate the access_token to make sure it's still valid
  try {
    if ( !$session->validate() ) {
      $session = null;
    }
  } catch ( Exception $e ) {
    // catch any exceptions
    $session = null;
  }
}  
 
if ( !isset( $session ) || $session === null ) {
  // no session exists
  
  try {
    $session = $helper->getSessionFromRedirect();
  } catch( FacebookRequestException $ex ) {
    // When Facebook returns an error
    // handle this better in production code
    print_r( $ex );
  } catch( Exception $ex ) {
    // When validation fails or other local issues
    // handle this better in production code
    print_r( $ex );
  }
  
}

// see if we have a session
if ( isset( $session ) ) 

{
  $currentdate = date('Y-m-d');
  //echo $currentdate;
  // save the session
  $_SESSION['fb_token'] = $session->getToken();
  // create a session using saved token or the new one we generated at login
  $session = new FacebookSession( $session->getToken() );
  
  // graph api request for user data
  $graphObject = (new FacebookRequest($session, 'GET', '/me?fields=feed'))->execute()->getGraphObject()->asArray();
  
  echo  '<pre>' . print_r( $graphObject, 1 ) . '</pre>';

/*if ($_GET["feed"] == "0")
  { 
    $number_of_feed = 0; 
    echo $_GET["feed"];
  }
elseif ($_GET["feed"] == "all_feed")
  {
    $numberof_feed = count($graphObject['feed']->data);
    echo $_GET["feed"];
  }
  else 
  {
    $number_of_feed == $_GET["number_of_feed"];
    echo $_GET["feed"];
  } */

$number_of_feed_items = count($graphObject['feed']->data);
$feednumber = 0;
//echo $feednumber;
//echo $number_of_feed_items;

$number_of_To_Names = count($graphObject[$feednumber]->data->to->data);
$ToFacebooknumber = 0;


$number_of_With_Names = count($graphObject['from']->data->story_tags->data);
$storytagsFacebooknumber = 0;


$number_of_likes = count($graphObject['from']->data->likes->data);
$LikesFacebooknumber = 0;


$number_of_comments = count($graphObject['from']->data->comments->data);
$CommentsFacebooknumber = 0;


 while ($feednumber <= $number_of_feed_items)
  $feednumber ++;
//echo $feednumber;
  {
  	if (isset ($graphObject['feed']->data[$feednumber]->id))
      {
        $Record_ID = $graphObject['feed']->data[$feednumber]->id;     
      }
    if (isset ($graphObject['feed']->data[$feednumber]->from->name))
      {
        $FacebookAccountName = $graphObject['feed']->data[$feednumber]->from->name;  
         
      }
    /*while ($storytagsFacebooknumber < $number_of_To_Names)
      {
        if (isset ($graphObject['feed']->data[$feednumber]->to->data[0]->name))
          {
            $ToFacebookAccountName = $graphObject['feed']->data[$feednumber]->to->data[0]->name;
          }
        if (isset ($graphObject['feed']->data[$feednumber]->with_tags->data[0]->name))
          {
            $WithFacebookAccountName = $graphObject['feed']->data[$feednumber]->with_tags->data[0]->name;
          }
          $storytagsFacebooknumber++;
      }
      */
    if (isset ($graphObject['feed']->data[$feednumber]->story))
      {
        $FeedStory = $graphObject['feed']->data[$feednumber]->story;
      
      }
    else
    {
      $FeedStory = "N/A";
    }

    /*while ($WithFacebooknumber < $number_of_With_Names)
      {
        if (isset ($graphObject['feed']->data[$feednumber]->story_tags[0]->name))
          {
            $StoryTagsName = $graphObject['feed']->data[$feednumber]->story_tags[0]->name;
          }
        if (isset ($graphObject['feed']->data[$feednumber]->story_tags[0]->type))
          {
            $StoryTagsType = $graphObject['feed']->data[$feednumber]->story_tags[0]->type;
          }
      }
      */
      if (isset ($graphObject['feed']->data[$feednumber]->picture))
      {
        $TaggedPicture = $graphObject['feed']->data[$feednumber]->picture;
        $taglength = count($TaggedPicture);
      }
      if ($taglength > 44) 
      {
        $TaggedPicture = "url too long";
        echo $TaggedPicture;
      }
      if (isset ($graphObject['feed']->data[$feednumber]->link))
      {
        $PictureLink = $graphObject['feed']->data[$feednumber]->link;
        $urllength = count($PictureLink);
        
      }
      if ($urllength > 44) 
      {
        $PictureLink = "url too long";
      } 

      if (isset ($graphObject['feed']->data[$feednumber]->description))
      {
        $PictureDescription = $graphObject['feed']->data[$feednumber]->description;
      }
      if (isset ($graphObject['feed']->data[$feednumber]->privacy->value))
      {
        $PrivacyType = $graphObject['feed']->data[$feednumber]->privacy->value;
      }
      if (isset ($graphObject['feed']->data[$feednumber]->type))
      {
        $FeedType = $graphObject['feed']->data[$feednumber]->type;
      }  
      if (isset ($graphObject['feed']->data[$feednumber]->status_type))
      {
        $FeedStatusType = $graphObject['feed']->data[$feednumber]->status_type;
      }
      if (isset ($graphObject['feed']->data[$feednumber]->application->name))
      {
        $FeedApplicationName = $graphObject['feed']->data[$feednumber]->application->name;
      }
      else
      {
        $FeedApplicationName = "Facebook";
      } 
      
      while ($WithFacebooknumber < $number_of_likes)
      {
        if (isset ($graphObject['feed']->data[$feednumber]->likes->data[0]->name))
          {
            $FacebookLikesName = $graphObject['feed']->data[$feednumber]->likes->data[0]->name;
          }
          $LikesFacebooknumber++
      }
      while ($CommentsFacebooknumber < $number_of_comments)
      {
        if (isset ($graphObject['feed']->data[$feednumber]->comments->data[0]->from->name))
          {
            $FeedMessageFromName = $graphObject['feed']->data[$feednumber]->comments->data[0]->from->name;
          }

          }
          
        
        
      if (isset ($graphObject['feed']->data[$feednumber]->message))
      {
          $FeedMessage = $graphObject['feed']->data[$feednumber]->message;
          echo $FeedMessage;
      }
          else
    {
      $FeedStory = "N/A";
    }
      if (isset ($graphObject['feed']->data[$feednumber]->created_time))
      {
        $CreatedTime = $graphObject['feed']->data[$feednumber]->created_time;
        echo $CreatedTime;
      }
      if (isset ($graphObject['feed']->data[$feednumber]->updated_time))
      {
        $UpdatedTime = $graphObject['feed']->data[$feednumber]->updated_time;
      } 
      if (isset ($graphObject['feed']->data[$feednumber]->comments->like_count))
      {
        $LikeCount = $graphObject['feed']->data[$feednumber]->comments->like_count;
      } 
      else
      {
        $LikeCount = "N/A";
      } 
        
        
      

        if (isset ($feedtart))
      {
        $feedstartTimestamp = strtotime($feedstart);
      }  

//echo $feedstart;
//echo $feedstartTimestamp;
//converts time into unix timestamp
      if (isset ($eventEnd))
      {
        $feedupdateTimestamp = strtotime($UpdatedTime);
      }  
      else
      { 
        $eventEndTimestamp = $feedtartTimestamp;
      } 

//works out event time type
$currentdate = date('Y-m-d');
$currentDateTimestamp = strtotime($currentdate);


//Post fields
echo $FacebookAccountName;
echo $Record_ID;
echo $FeedStory;
echo $TaggedPicture;
$fullName = preg_split('/\s+/', $FacebookAccountName);

      
$eventsucess = "sucess";
}

if ($eventsucess = "sucess" ) {
  //echo 'Your Data is being pushed via the HAT API to your HAT';
}
else 
  // print logout url using session and redirect_uri (logout.php page should destroy the session)
  echo '<a href="' . $helper->getLogoutUrl( $session, 'http://hubofallthings.com/app/HAT-Apps/facebook/src' ) . '">Logout</a>';
  
} else {
  // show login url
  echo '<a href="' . $helper->getLoginUrl( array( 'email', 'user_friends', 'user_events', 'publish_stream', 'read_stream' ) ) . '">Login</a>';
}

?>