<?php
 
	require_once '../include/DbHandler.php';
	require_once '../include/PassHash.php';
	require '.././libs/Slim/Slim.php';
	 
	\Slim\Slim::registerAutoloader();
	 	
	$app = new \Slim\Slim();
	
	// User id from db - Global Variable
	$user_id = NULL;
	 
	// Define Goovy Playables Directory 
	define(GOOVY_DIR_NAME , "uploaded_goovies/");
	define(GOOVY_DIR_URL, $_SERVER['SERVER_NAME'] . "/" . GOOVY_DIR_NAME);
	define(GOOVY_DIR, $_SERVER["DOCUMENT_ROOT"] . GOOVY_DIR_NAME); 

	 
	/**
	 * Verifying required params posted or not
	 */
	function verifyRequiredParams($required_fields) {
		$error = false;
		$error_fields = "";
		$request_params = array();
		$request_params = $_REQUEST;
		// Handling PUT request params
		if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
			$app = \Slim\Slim::getInstance();
			parse_str($app->request()->getBody(), $request_params);
		}
		foreach ($required_fields as $field) {
			if (!isset($request_params[$field]) || strlen(trim($request_params[$field])) <= 0) {
				$error = true;
				$error_fields .= $field . ', ';
			}
		}
	 
		if ($error) {
			// Required field(s) are missing or empty
			// echo error json and stop the app
			$response = array();
			$app = \Slim\Slim::getInstance();
			$response["error"] = true;
			$response["message"] = 'Required field(s) ' . substr($error_fields, 0, -2) . ' is missing or empty';
			echoRespnse(400, $response);
			$app->stop();
		}
	}
	
	
	//on beach add -> update goovies beaches.
	function updateGooviesBeachId($db){
		$result = $db->getAllGoovies();

		// looping through result and updating goovies array
		while ($goovy = $result->fetch_assoc()) {			
			$id = $goovy["id"];
			$lat = $goovy["lat"];
			$lon = $goovy["lon"];
			$beach_id = $goovy["beach_id"];
			$new_beach_id = goovyGPStoBeachId($db, $lat, $lon);
			
			if($beach_id != $new_beach_id){
				$db->updateGoovyBeach($id, $new_beach_id);
			}
		}
	}
	
	// Can do it Better!
	function goovyGPStoBeachId($db, $lat, $lon){
		$beachesGPS = $db->getAllBeachesGPSdata();
		$nearBeachesIds = array();
		// A very Big CONST
		$BIGCONST = 100000000000000000000;
		$closestRange = $BIGCONST;
		$closestId;
				 
		foreach ($beachesGPS as $beachData) {
			//LAT
			$northEastLat = $beachData["northeast_lat"];
			$southWestLat = $beachData["southwest_lat"];
			
			//LON
			$northEastLon = $beachData["northeast_lon"];
			$southWestLon = $beachData["southwest_lon"];
			
			$isLatInRange = isInRange($lat, $northEastLat, $southWestLat);
			$isLonInRange = isInRange($lon, $northEastLon, $southWestLon);
			
			if($isLatInRange && $isLonInRange){
				//return $beachData["id"];
				array_push($nearBeachesIds, $beachData["id"]);
			}
			
			/// NEAREST START ///
			$locationLat = $beachData["location_lat"];
			$locationLon = $beachData["location_lon"];

			$range = LocationsDistance($lat, $lon, $locationLat, $locationLon);
			
			if($range <= $closestRange){
				$closestRange = $range;
				$closestId = $beachData["id"];
			}
			
			/// NEAREST END ///
		}
		
		return $closestId;
	}
	
	function LocationsDistance($lat, $lon, $locationLat, $locationLon){
		$latD = $lat - $locationLat;
		$lonD = $lon - $locationLon;
		$dPow = pow($latD, 2) + pow($lonD, 2); 
		return sqrt($dPow);
	}
	
	function isInRange($num, $rangeA, $rangeB){
		if($rangeA > $rangeB){
			$max = $rangeA;
			$min = $rangeB;
		}else{
			$max = $rangeB;
			$min = $rangeA;
		}
		
		if($num <= $max && $num >= $min){
			return true;
		}
		
		return false;
	}
	
	 
	/**
	 * Validating email address
	 */
	function validateEmail($email) {
		$app = \Slim\Slim::getInstance();
		if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
			$response["error"] = true;
			$response["message"] = 'Email address is not valid';
			echoRespnse(400, $response);
			$app->stop();
		}
	}
	 
	/**
	 * Echoing json response to client
	 * @param String $status_code Http response code
	 * @param Int $response Json response
	 */
	function echoRespnse($status_code, $response) {
		$app = \Slim\Slim::getInstance();
		// Http response code
		$app->status($status_code);
	 
		// setting response content type to json
		$app->contentType('application/json; charset=utf-8');
	 
		echo json_encode( $response, JSON_UNESCAPED_UNICODE );
	}
	
	/**
	 * Adding Middle Layer to authenticate every request
	 * Checking if the request has valid api key in the 'Authorization' header
	 */
	function authenticate(\Slim\Route $route) {
		// Getting request headers
		$headers = apache_request_headers();
		
		$auth;
		foreach ($headers as $header => $value) {
			if ($header == "authorization"){
				$auth = $value;
			}
		}
		
		$response = array();
		$app = \Slim\Slim::getInstance();
				
		// Verifying Authorization Header
		//if (isset($headers['Authorization'])) {
		if (isset($auth)){
			$db = new DbHandler();
			// get the api key
			//$api_key = $headers['authorization'];
			$api_key = $auth;
			// validating api key
			if (!$db->isValidApiKey($api_key)) {
				// api key is not present in users table
				$response["error"] = true;
				$response["message"] = "Access Denied. Invalid Api key";
				echoRespnse(401, $response);
				$app->stop();
			} else {
				global $user_id;
				// get user primary key id
				$user = $db->getUserId($api_key);
				if ($user != NULL){
					$user_id = $user["id"];
				}	
			}
		} else {
			print_r($headers);
			// api key is missing in header
			$response["error"] = true;
			$response["message"] = "Api key is misssing";
			echoRespnse(400, $response);
			$app->stop();
		}
	}
	
	// HELLO
	
	$app->post('/hello', function () use ($app) {
		$body = $app->request->getBody();
		echo $body;
	});
	
	/**
	 * User Registration
	 * url - /register
	 * method - POST
	 * params - name, email, password
	 */
	$app->post('/register', function() use ($app) {
				
		//verifyRequiredParams(array('name', 'email', 'password'));
		
		$json_body = $app->request->getBody();
		$params = json_decode($json_body);
				
		$name = $params->name;
		$email = $params->email;
		$password = $params->password;
		
		//echo '{"error": false, "message": "You are successfully registered"}';
		
		$response = array();

		// validating email address
		validateEmail($email);
				
		$db = new DbHandler();
				
		$res = $db->createUser($name, $email, $password);
				
		if ($res == USER_CREATED_SUCCESSFULLY) {
			$response["error"] = false;
			$response["message"] = "You are successfully registered";
			echoRespnse(201, $response);
		} else if ($res == USER_CREATE_FAILED) {
			$response["error"] = true;
			$response["message"] = "Oops! An error occurred while registereing";
			echoRespnse(200, $response);
		} else if ($res == USER_ALREADY_EXISTED) {
			$response["error"] = true;
			$response["message"] = "Sorry, this email already existed";
			echoRespnse(200, $response);
		}
					
	});
	
	
	/**
	 * User Login
	 * url - /login
	 * method - POST
	 * params - email, password
	 */
	$app->post('/login', function() use ($app) {

		// check for required params
		//verifyRequiredParams(array('email', 'password'));
		
		// reading post params
		$json_body = $app->request->getBody();
		$params = json_decode($json_body);
				
		$email = $params->email;
		$password = $params->password;

		$response = array();

		$db = new DbHandler();
		// check for correct email and password
		if ($db->checkLogin($email, $password)) {
			// get the user by email
			$user = $db->getUserByEmail($email);

			if ($user != NULL) {
				$response["error"] = false;
				$response['name'] = $user['name'];
				$response['email'] = $user['email'];
				$response['apiKey'] = $user['api_key'];
				$response['createdAt'] = $user['created_at'];
			} else {
				// unknown error occurred
				$response['error'] = true;
				$response['message'] = "An error occurred. Please try again";
			}
		} else {
			// user credentials are wrong
			$response['error'] = true;
			$response['message'] = 'Login failed. Incorrect credentials';
		}

		echoRespnse(200, $response);
	});
	
	
	
	$app->post('/goovy', 'authenticate', function() use ($app) {
		// check for required params
		//verifyRequiredParams(array('goovy'));

		$response = array();
		
		$json_body = $app->request->getBody();
		//echo $json_body;
		$params = json_decode($json_body);
				
		$goovy = $params->goovy;
		$lat = $params->lat;
		$lon = $params->lon;
		$description = $params->description;
		$height = $params->height;
		$crowed = $params->crowed;
		
		$mp4Content = base64_decode($goovy);
		
		$goovy_unique = md5(rand(10,99) . time());
		$goovyName = $goovy_unique . ".mp4";
		$goovyPath = GOOVY_DIR . $goovyName;
		
		global $user_id;

		$db = new DbHandler();
		
		//  Check Beach By Lat & Lon  //
		$beach_id = goovyGPStoBeachId($db, $lat, $lon); // Returns 1

		// creating new goovy
		$goovy_id = $db->createGoovy($user_id, $goovyName, $lat, $lon, $beach_id, $description, $height, $crowed);

		if ($goovy_id != NULL) {
			// Only if Could Create Goovy, Insert Playable
			$newGoovy = fopen($goovyPath, "w");
			fwrite($newGoovy, $mp4Content);
			fclose($newGoovy);
			
			// Send Response
			$response["error"] = false;
			$response["message"] = "goovy created successfully";
			$response["goovy_id"] = $goovy_id;
			echoRespnse(201, $response);
		} else {
			$response["error"] = true;
			$response["message"] = "Failed to create goovy. Please try again";
			$response["goovy_id"] = null;
			echoRespnse(500, $response);
		}
		
	});
	

	
	/**
	 * Listing all goovies of particual user
	 * method GET
	 * url /goovies          
	 */
	 	 
	$app->get('/goovies/user', 'authenticate', function() {
		global $user_id;
		$response = array();
		$db = new DbHandler();

		// fetching all user goovies
		$result = $db->getAllUserGoovies($user_id);

		$response["error"] = false;
		$response["goovies"] = array();

		// looping through result and preparing goovies array
		while ($goovy = $result->fetch_assoc()) {			
			$tmp = array();
			// Playable URL
			$url = "http://" . GOOVY_DIR_URL . $goovy["playable"];
			$beach_name = $db->getBeachNameById($goovy["beach_id"]);
			
			$tmp["url"] = $url;
			$tmp["id"] = $goovy["id"];
			$tmp["lat"] = $goovy["lat"];
			$tmp["lon"] = $goovy["lon"];
			$tmp["beach_id"] = $goovy["beach_id"];
			$tmp["beach_name"] = $beach_name;
			$tmp["description"] = $goovy["description"];
			$tmp["height"] = $goovy["height"];
			$tmp["crowed"] = $goovy["crowed"];
			$tmp["createdAt"] = $goovy["created_at"];
			array_push($response["goovies"], $tmp);
		}

		echoRespnse(200, $response);
	});
		
		
	/**
	 * Listing goovies of particual beach
	 * method GET
	 * url /goovies/:id
	 */
	 
	$app->get('/goovies/beach/:id', 'authenticate', function($beach_id) use ($app){
		global $user_id;
		$response = array();
		$db = new DbHandler();

		// fetch goovies
		$result = $db->getGooviesByBeachId($beach_id);
		
		$response["error"] = false;
		$response["goovies"] = array();

		// looping through result and preparing goovies array
		while ($goovy = $result->fetch_assoc()) {			
			$tmp = array();
			// Playable URL
			$url = "http://" . GOOVY_DIR_URL . $goovy["playable"];
			$beach_name = $db->getBeachNameById($goovy["beach_id"]);
			
			$tmp["id"] = $goovy["id"];
			$tmp["url"] = $url;
			$tmp["lat"] = $goovy["lat"];
			$tmp["lon"] = $goovy["lon"];
			$tmp["beach_id"] = $goovy["beach_id"];			
			$tmp["beach_name"] = $beach_name;
			$tmp["description"] = $goovy["description"];
			$tmp["height"] = $goovy["height"];
			$tmp["crowed"] = $goovy["crowed"];
			$tmp["createdAt"] = $goovy["created_at"];
			array_unshift($response["goovies"], $tmp);
		}
		
		echoRespnse(200, $response);
	});

	
	$app->get('/goovies/all', 'authenticate', function() {
		global $user_id;
		$response = array();
		$db = new DbHandler();

		// fetching all goovies
		$result = $db->getAllGoovies();

		$response["error"] = false;
		$response["goovies"] = array();

		// looping through result and preparing goovies array
		while ($goovy = $result->fetch_assoc()) {			
			$tmp = array();
			// Playable URL
			$url = "http://" . GOOVY_DIR_URL . $goovy["playable"];
			$beach_name = $db->getBeachNameById($goovy["beach_id"]);
			
			$tmp["id"] = $goovy["id"];
			$tmp["url"] = $url;
			$tmp["lat"] = $goovy["lat"];
			$tmp["lon"] = $goovy["lon"];
			$tmp["beach_id"] = $goovy["beach_id"];
			$tmp["beach_name"] = $beach_name;
			$tmp["description"] = $goovy["description"];
			$tmp["height"] = $goovy["height"];
			$tmp["crowed"] = $goovy["crowed"];
			$tmp["createdAt"] = $goovy["created_at"];
			//array_push($response["goovies"], $tmp);
			array_unshift($response["goovies"], $tmp);
		}
		

		echoRespnse(200, $response);
	});
	
	/*
	$app->post('/beaches', 'authenticate', function() use ($app) {
		// check for required params
		//verifyRequiredParams(array('goovy'));

		$response = array();
		
		$json_body = $app->request->getBody();
		$params = json_decode($json_body);
				
		$name = $params->name;
		$locationLat = $params->locationLat;
		$locationLon = $params->locationLon;
		$northEastLat = $params->northEastLat;
		$northEastLon = $params->northEastLon;
		$southWestLat = $params->southWestLat;
		$southWestLon = $params->southWestLon;
		
		global $user_id;

		$db = new DbHandler();

		$beach_id = $db->isBeachExists($name);
		if ($beach_id)
		{
			// Check if user doesn't have it
			if(!$db->doesUserHaveBeach($user_id, $beach_id)){
				$db->createUserBeach($user_id, $beach_id);
				$response["error"] = false;
				$response["message"] = "beach associated to user successfully";
				$response["beach_id"] = $beach_id;
				echoRespnse(200, $response);
			} else {
				// User has beach already.
				$response["error"] = false;
				$response["message"] = "beach already associated to user";
				$response["beach_id"] = $beach_id;
				echoRespnse(200, $response);
			}
		}
		else{
			// Beach doesn't Exist - Create it.
			$beach_id = $db->createBeach($user_id, $name, $locationLat, $locationLon, $northEastLat, $northEastLon, $southWestLat, $southWestLon);
			if($beach_id != NULL){
				$response["error"] = false;
				$response["message"] = "beach created successfully";
				$response["beach_id"] = $beach_id;
				echoRespnse(201, $response);
			}
			else{
				$response["error"] = true;
				$response["message"] = "Failed to create beach. Please try again";
				$response["beach_id"] = -1;
				echoRespnse(500, $response);
			}
		}
	});
	*/
	
	$app->post('/beach/add', 'authenticate', function() use ($app) {
		// check for required params
		//verifyRequiredParams(array('goovy'));
		
		$json_body = $app->request->getBody();
		$params = json_decode($json_body);
		$beach_name = $params->address;
		
		$response = array();
		global $user_id;
		$db = new DbHandler();
		
		$jsonGeocodeResponse = file_get_contents('https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyAWAnL4eDFoniJjLj-Ddt6J_JRB4q6gI_s&address=' . urlencode($beach_name));
		
		$params = json_decode($jsonGeocodeResponse);
		
		$beach;
		
		if($params->status == "OK"){															
			$results = $params->results;
			if(count($results) > 1){
				//$beach = null;
				$beach = $results[0];
			}else{
				$beach = $results[0];
			}
		}
		
		$isBeach = true; //in_array("natural_feature", $beach->types);
		if(isset($beach) && $isBeach){
			// Create Beach		
			$place_id = $beach->place_id;
			$beach_id = $db->isBeachExists($place_id);
						
			if ($beach_id)
			{
				// Check if user doesn't have it
				if(!$db->doesUserHaveBeach($user_id, $beach_id)){
					$db->createUserBeach($user_id, $beach_id);
					$beach_name = $db->getBeachNameById($beach_id);
					
					$response["error"] = false;
					$response["message"] = "beach associated to user successfully";
					$response["beach_id"] = $beach_id;
					$response["beach_name"] = $beach_name;
					echoRespnse(200, $response);
				} else {
					// User has beach already.
					$beach_name = $db->getBeachNameById($beach_id);
					
					$response["error"] = false;
					$response["message"] = "beach already associated to user";
					$response["beach_id"] = $beach_id;
					$response["beach_name"] = $beach_name;
					echoRespnse(200, $response);
				}
			} else{
				// Beach doesn't Exist - Create it.
				$gotParams = true;
				try{
					$name = $beach->formatted_address;
					$geometry = $beach->geometry;
					
					$locationLat = $geometry->location->lat;
					$locationLon = $geometry->location->lng;
					
					$bounds = $geometry->bounds;
					$northEastLat = $bounds->northeast->lat;
					$northEastLon = $bounds->northeast->lng;
					$southWestLat = $bounds->southwest->lat;
					$southWestLon = $bounds->southwest->lng;
				} catch(Exception $e){
					// Cancel
					$gotParams = false;
					$response["error"] = true;
					$response["message"] = "Couldn't interpret beach name";
					$response["beach_id"] = -1;
					$response["beach_name"] = "None";
					echoRespnse(400, $response);
				} 
				
				if($gotParams){
					$beach_id = $db->createBeach($user_id, $name, $place_id, $locationLat, $locationLon, $northEastLat, $northEastLon, $southWestLat, $southWestLon);
				
					if($beach_id != NULL){
						
						// Renew beach_id for goovies
						updateGooviesBeachId($db);
						
						$beach_name = $db->getBeachNameById($beach_id);
						$response["error"] = false;
						$response["message"] = "beach created successfully";
						$response["beach_id"] = $beach_id;
						$response["beach_name"] = $beach_name;
						echoRespnse(201, $response);
					}
					else{
						$response["error"] = true;
						$response["message"] = "Failed to create beach. Please try again";
						$response["beach_id"] = -1;
						$response["beach_name"] = "None";
						echoRespnse(500, $response);
					}
				}	
			}
		}else{
			// Cancel
			$response["error"] = true;
			$response["message"] = "Too many results. Beach name was too general";
			$response["beach_id"] = -1;
			echoRespnse(400, $response);
		}
	});
	
	// Get user's Beaches
	$app->get('/user/beaches', 'authenticate', function() use ($app) {
		// check for required params
		//verifyRequiredParams(array('goovy'));

		$response = array();
		
		global $user_id;

		$db = new DbHandler();
		$beach_ids = $db->getUserBeachesIds($user_id);
		
		if($beach_ids){
			$beaches = $db->getBeachesByIds($beach_ids);
			
			$response["error"] = false;
			$response["beaches"] = array();

			// looping through beaches and preparing beaches array
			foreach ($beaches as $beach) {			
				$tmp = array();
				$tmp["id"] = $beach["id"];
				$tmp["name"] = $beach["name"];
				array_unshift($response["beaches"], $tmp);
			}
			
			echoRespnse(200, $response);
		}else{
			// No beaches
			$response["error"] = false;
			$response["beaches"] = NULL;
			echoRespnse(200, $response);
		}
	});

	$app->run();
?>