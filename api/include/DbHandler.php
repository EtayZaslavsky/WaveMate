<?php
 
/**
 * Class to handle all db operations
 * This class will have CRUD methods for database tables
 *
 */
class DbHandler {
 
    private $conn;
 
    function __construct() {
        require_once 'DbConnect.php';
        // opening db connection
        $db = new DbConnect();
        $this->conn = $db->connect();
    }
 
    /* ------------- `users` table method ------------------ */
 
    /**
     * Creating new user
     * @param String $name User full name
     * @param String $email User login email id
     * @param String $password User login password
     */
    public function createUser($name, $email, $password) {
        require_once 'PassHash.php';
        $response = array();
 
        // First check if user already existed in db
        if (!$this->isUserExists($email)) {
            // Generating password hash
            $password_hash = PassHash::hash($password);
 
            // Generating API key
            $api_key = $this->generateApiKey();
 
            // insert query
            $stmt = $this->conn->prepare("INSERT INTO users(name, email, password_hash, api_key) values(?, ?, ?, ?)");
            $stmt->bind_param("ssss", $name, $email, $password_hash, $api_key);
 
            $result = $stmt->execute();
 
            $stmt->close();
 
            // Check for successful insertion
            if ($result) {
                // User successfully inserted
                return USER_CREATED_SUCCESSFULLY;
            } else {
                // Failed to create user
                return USER_CREATE_FAILED;
            }
        } else {
            // User with same email already existed in the db
            return USER_ALREADY_EXISTED;
        }
 
        return $response;
    }
 
    /**
     * Checking user login
     * @param String $email User login email id
     * @param String $password User login password
     * @return boolean User login status success/fail
     */
    public function checkLogin($email, $password) {
        // fetching user by email
        $stmt = $this->conn->prepare("SELECT password_hash FROM users WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        $stmt->execute();
 
        $stmt->bind_result($password_hash);
 
        $stmt->store_result();
 
        if ($stmt->num_rows > 0) {
            // Found user with the email
            // Now verify the password
 
            $stmt->fetch();
 
            $stmt->close();
 
            if (PassHash::check_password($password_hash, $password)) {
                // User password is correct
                return TRUE;
            } else {
                // user password is incorrect
                return FALSE;
            }
        } else {
            $stmt->close();
 
            // user not existed with the email
            return FALSE;
        }
    }
 
    /**
     * Checking for duplicate user by email address
     * @param String $email email to check in db
     * @return boolean
     */
    private function isUserExists($email) {
        $stmt = $this->conn->prepare("SELECT id from users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }
 
    /**
     * Fetching user by email
     * @param String $email User email id
     */
    public function getUserByEmail($email) {
        $stmt = $this->conn->prepare("SELECT name, email, api_key, created_at FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $user;
        } else {
            return NULL;
        }
    }
 
    /**
     * Fetching user api key
     * @param String $user_id user id primary key in user table
     */
    public function getApiKeyById($user_id) {
        $stmt = $this->conn->prepare("SELECT api_key FROM users WHERE id = ?");
        $stmt->bind_param("i", $user_id);
        if ($stmt->execute()) {
            $api_key = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $api_key;
        } else {
            return NULL;
        }
    }
 
    /**
     * Fetching user id by api key
     * @param String $api_key user api key
     */
    public function getUserId($api_key) {
        $stmt = $this->conn->prepare("SELECT id FROM users WHERE api_key = ?");
        $stmt->bind_param("s", $api_key);
        if ($stmt->execute()) {
            $user_id = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $user_id;
        } else {
            return NULL;
        }
    }
 
    /**
     * Validating user api key
     * If the api key is there in db, it is a valid key
     * @param String $api_key user api key
     * @return boolean
     */
    public function isValidApiKey($api_key) {
        $stmt = $this->conn->prepare("SELECT id from users WHERE api_key = ?");
        $stmt->bind_param("s", $api_key);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }
 
    /**
     * Generating random Unique MD5 String for user Api key
     */
    private function generateApiKey() {
        return md5(uniqid(rand(), true));
    }
 
    /* ------------- `goovies` table method ------------------ */
 
    /**
     * Creating new goovy
     * @param String $user_id user id to whom goovy belongs to
     * @param String $goovy goovy's playable name
	 * @param String $lat GPS Latitude
	 * @param String $lon GPS Longtitude
	 * @param String $description description for the Goovy
	 * @param String $height wave height
	 * @param String $crowed how crowded is it
     */
    public function createGoovy($user_id, $playable, $lat, $lon, $beach_id, $description, $height, $crowed) {        
        $stmt = $this->conn->prepare("INSERT INTO goovies(playable, lat, lon, beach_id, description, height, crowed) VALUES(?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("sssisss", $playable, $lat, $lon, $beach_id, $description, $height, $crowed);
        $result = $stmt->execute();
        $stmt->close();
 
        if ($result) {
            // goovy row created
            // now assign the goovy to user
            $new_goovy_id = $this->conn->insert_id;
            $res = $this->createUserGoovy($user_id, $new_goovy_id);
            if ($res) {
                // goovy created successfully
                return $new_goovy_id;
            } else {
                // goovy failed to create
                return NULL;
            }
        } else {
            // goovy failed to create
            return NULL;
        }
    }
	
	
	public function updateGoovyBeach($goovy_id, $new_beach_id) {        
        $stmt = $this->conn->prepare("UPDATE goovies SET beach_id = ? WHERE id = ?");
        $stmt->bind_param("ii", $new_beach_id, $goovy_id);
        $result = $stmt->execute();
        $stmt->close();
 
        if ($result) {
            return true;
        } else {
            // failed to update beach id
            return false;
        }
    }
	
 
    /**
     * Fetching single goovy
     * @param String $goovy_id id of the goovy
     */
    public function getGoovy($goovy_id, $user_id) {
        $stmt = $this->conn->prepare("SELECT t.id, t.playable, t.lat, t.lon, t.description, t.height, t.crowed, t.created_at from goovies t, user_goovies ut WHERE t.id = ? AND ut.goovy_id = t.id AND ut.user_id = ?");
        $stmt->bind_param("ii", $goovy_id, $user_id);
        if ($stmt->execute()) {
            $goovy = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $goovy;
        } else {
            return NULL;
        }
    }
 
    /**
     * Fetching all user goovies
     * @param String $user_id id of the user
     */
    public function getAllUserGoovies($user_id) {
        $stmt = $this->conn->prepare("SELECT t.* FROM goovies t, user_goovies ut WHERE t.id = ut.goovy_id AND ut.user_id = ?");
        $stmt->bind_param("i", $user_id);
        $stmt->execute();
        $goovies = $stmt->get_result();
        $stmt->close();
        return $goovies;
    }
	
	
	public function getGooviesByBeachId($beach_id){
		$stmt = $this->conn->prepare("SELECT * FROM goovies WHERE beach_id = ?");
        $stmt->bind_param("i", $beach_id);
        $stmt->execute();
        $goovies = $stmt->get_result();
        $stmt->close();
        return $goovies;
	}
	
	
	/**
     * Fetching all goovies existing
     */
    public function getAllGoovies() {
        $stmt = $this->conn->prepare("SELECT * FROM goovies");
        $stmt->execute();
        $goovies = $stmt->get_result();
        $stmt->close();
        return $goovies;
    }
 
 
    /**
     * Deleting a goovy
     * @param String $goovy_id id of the goovy to delete
     */
    public function deleteGoovy($user_id, $goovy_id) {
        $stmt = $this->conn->prepare("DELETE t FROM goovies t, user_goovies ut WHERE t.id = ? AND ut.goovy_id = t.id AND ut.user_id = ?");
        $stmt->bind_param("ii", $goovy_id, $user_id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }
 
    /* ------------- `user_goovies` table method ------------------ */
 
    /**
     * Function to assign a goovy to user
     * @param String $user_id id of the user
     * @param String $goovy_id id of the goovy
     */
    public function createUserGoovy($user_id, $goovy_id) {
        $stmt = $this->conn->prepare("INSERT INTO user_goovies(user_id, goovy_id) values(?, ?)");
        $stmt->bind_param("ii", $user_id, $goovy_id);
        $result = $stmt->execute();
        $stmt->close();
        return $result;
    }
	
	
	    /* ------------- `beaches` table method ------------------ */
 
    /**
     * Creating new beach
     * @param String $user_id user id who entered beach
	 * @param String $name beach name
	 * @param String $lat GPS Latitude
	 * @param String $lon GPS Longtitude
     */
    public function createBeach($user_id, $name, $place_id, $locationLat, $locationLon, $northEastLat, $northEastLon, $southWestLat, $southWestLon) {        
        $stmt = $this->conn->prepare("INSERT INTO beaches(name, place_id, location_lat, location_lon, northeast_lat, northeast_lon, southwest_lat, southwest_lon) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("ssdddddd", $name, $place_id, $locationLat, $locationLon, $northEastLat, $northEastLon, $southWestLat, $southWestLon);
        $result = $stmt->execute();
        $stmt->close();
 
        if ($result) {
            // beach row created
            // now assign the beach to user
            $new_beach_id = $this->conn->insert_id;
            $res = $this->createUserBeach($user_id, $new_beach_id);
            if ($res) {
                // beach created successfully
                return $new_beach_id;
            } else {
                // beach failed to create
                return NULL;
            }
        } else {
            // beach failed to create
            return NULL;
        }
    }
	
	public function getBeachesByIds($beach_ids) {
		// SQL Injection!!!
		$query = "SELECT id, name, place_id, location_lat, location_lon, northeast_lat, northeast_lon, southwest_lat, southwest_lon FROM beaches WHERE id IN (";
		
		foreach ($beach_ids as $beach_id) {
			$query = $query . $beach_id. ", ";
		}
		
		$query = substr($query, 0, -2);
		$query = $query . ")";
				
        $stmt = $this->conn->prepare($query);		
		if ($stmt->execute()) {
			$res = $stmt->get_result();
			if ($res->num_rows > 0)
			{
				$beaches = array();
				while ($beach = $res->fetch_assoc()) {
					array_push($beaches, $beach);
				}
				$stmt->close();
				return $beaches;
				
			}else{
				$stmt->close();
				return FALSE;
			}
			
		} else {
            return FALSE;
        }
    }


	public function getAllBeachesGPSdata() {
		$query = "SELECT id, place_id, location_lat, location_lon, northeast_lat, northeast_lon, southwest_lat, southwest_lon FROM beaches";
				
        $stmt = $this->conn->prepare($query);		
		if ($stmt->execute()) {
			$res = $stmt->get_result();
			if ($res->num_rows > 0)
			{
				$beachesGPS = array();
				while ($beachData = $res->fetch_assoc()) {
					array_push($beachesGPS, $beachData);
				}
				$stmt->close();
				return $beachesGPS;
				
			}else{
				$stmt->close();
				return FALSE;
			}
			
		} else {
            return FALSE;
        }
    }
	
	
	public function getBeachNameById($beach_id) {
		$query = "SELECT name FROM beaches WHERE id = ?";
        $stmt = $this->conn->prepare($query);
		$stmt->bind_param("i", $beach_id);		
		
		if ($stmt->execute()) {
			$res = $stmt->get_result();
			if ($res->num_rows > 0)
			{
				$beachName = $res->fetch_assoc()["name"];
				$stmt->close();
				return $beachName;
				
			}else{
				$stmt->close();
				return FALSE;
			}
			
		} else {
            return FALSE;
        }
    }
	
	
	
	
	public function isBeachExists($place_id){
		$stmt = $this->conn->prepare("SELECT id FROM beaches WHERE place_id = ?");
        $stmt->bind_param("s", $place_id);
		if ($stmt->execute()) {
			// beach exists
			$res = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			if ($res)
			{
				return $res["id"];
			}
			else{
				return FALSE;
			}
		} else {
			// beach doesn't exist
            return FALSE;
        }
	}
	
	
	    /* ------------- `user_beaches` table method ------------------ */
 
    /**
     * Function to assign a beach to user
     * @param String $user_id id of the user
     * @param String $beach_id id of the beach
     */
    public function createUserBeach($user_id, $beach_id) {
        $stmt = $this->conn->prepare("INSERT INTO user_beaches(user_id, beach_id) values(?, ?)");
        $stmt->bind_param("ii", $user_id, $beach_id);
        $result = $stmt->execute();
        $stmt->close();
        return $result;
    }
	
	public function doesUserHaveBeach($user_id, $beach_id) {
        $stmt = $this->conn->prepare("SELECT id FROM user_beaches WHERE user_id = ? AND beach_id = ?");
        $stmt->bind_param("ii", $user_id, $beach_id);
		if ($stmt->execute()) {
			// beach exists
			$rows = $stmt->get_result()->num_rows;

			$stmt->close();
			if ($rows > 0)
			{
				return TRUE;
			}
			else{
				return FALSE;
			}
		} else {
			// beach doesn't exist
            return FALSE;
        }
    }
	
	
	public function getUserBeachesIds($user_id) {
        $stmt = $this->conn->prepare("SELECT beach_id FROM user_beaches WHERE user_id = ?");
        $stmt->bind_param("i", $user_id);
		if ($stmt->execute()) {
			$res = $stmt->get_result();
			if ($res->num_rows > 0)
			{
				$ids = array();
				while ($id = $res->fetch_assoc()["beach_id"]) {
					array_push($ids, $id);
				}
				$stmt->close();
				return $ids;
				
			}else{
				$stmt->close();
				return FALSE;
			}
			
		} else {
            return FALSE;
        }
    }
	
 
}
 
?>