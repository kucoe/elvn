<?php

class Resp {
	public $error;
	public $value;
	 
	public function __construct($value, $error) {
		$this->value = $value;
		$this->error = $error;
	}
}

function getTimestamp() {
	$seconds = microtime(true);
	return round( ($seconds * 1000) );
}

function userExists($id) {
	return $id != 0 && file_exists("data/".$id);
}

function checkInitDirs() {
	if (!file_exists("data")) {
		mkdir("data");
		file_put_contents("data/id", "");
	}
	if (!file_exists("data/tokens")) {
		mkdir("data/tokens");
	}
}

function userId($email) {
	checkInitDirs();
	$handle = @fopen("data/id", "r");
	if ($handle) {
		while (!feof($handle)){
			$buffer = fgets($handle);
			if(strpos($buffer, $email) !== FALSE) {
				fclose($handle);
				return trim(str_replace($email, '', $buffer));
			}
		}
		fclose($handle);
	}
	return 0;
}

function newUserId($email) {
	checkInitDirs();
	$handle = @fopen("data/id", "a+");
	if ($handle) {
		$id = getTimestamp();
		fwrite($handle, $email.$id."\n");
		fclose($handle);
		return $id;
	}
	return 0;
}

function auth($email, $key) {
	$id = userId($email);
	$filename = "data/".$id."/key";
	$content = file_get_contents($filename);
	return $content && strcmp($content, $key) == 0;
}

function newUser($email, $key) {
	$id = userId($email);
	if($id == 0) {
		$id - newUserId($email);
	}
	mkdir("data/".$id, 0755);
	writeConfig($email, "");
	$filename = "data/".$id."/key";
	file_put_contents($filename, $key);
	return $id;
}

function deleteUser($email) {
	$id = userId($email);
	rmdir("data/".$id);
	return $id;
}

function getRaw() {
	$rawInput = @file_get_contents('php://input');
	if (!$rawInput) {
		$rawInput = '';
	}
}

function readConfig($email) {
	$id = userId($email);
	$filename = "data/".$id."/config.json";
	return file_get_contents($filename);
}

function writeConfig($email, $config) {
	$id = userId($email);
	$filename = "data/".$id."/config.json";
	return file_put_contents($filename, $config);
}

function nextToken($email, $key) {
	checkInitDirs();
	$characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
	$randomString = '';
	for ($i = 0; $i < 32; $i++) {
		$randomString .= $characters[rand(0, strlen($characters) - 1)];
	}
	//uniqid(md5(rand()), true)
	$filename = "data/tokens/" + $randomString;
	file_put_contents($filename, $email."|".$key);
	return $randomString;
}

function readToken($token) {
	$filename = "data/tokens/" + $token;
	if(file_exists($filename)) {
		$arr = explode("|", file_get_contents($filename));
		unlink($filename);
		return $arr;
	}
	return null;
}

function resp($value, $error=null) {
	echo json_encode(new Resp($value, $error));
}

?>