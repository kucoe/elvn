<?php
require_once('params.php');

if($key && auth($email, $key)) {
	if($config) {
		resp(writeConfig($email, $config) ? "true" : "false");
	} else {
		resp(readConfig($email));
	}
} else {
	resp("fail", "authentication failed");
}

?>