<?php 
require_once('lib.php');

$isDelete = $_SERVER['REQUEST_METHOD'] === 'DELETE';

function param($name, $get = false) {
	$res = $_POST[$name];
	if(!$res && $get) {
		$res = $_GET[$name];
	}
	return urldecode($res);
}

$token = param('token', true);

if($token && $token != '0') {
	$arr = readToken($token);
	if(!$arr) {
		resp("fail", "token is not valid");
		exit();
	}
	$email = $arr[0];
	$key = $arr[1];
} else {
	$email = param('email', true);
	if(!$email) {
		resp('', 'no email specified');
		exit();
	}
	if(!filter_var($email, FILTER_VALIDATE_EMAIL)) {
		resp('', 'invalid email specified');
		exit();
	}
	$key = param('key');
}
$items = param('items');




?>