<?php
require_once('params.php');

if($key && auth($email, $key)) {
	if($items) {
		resp(writeItems($email, $items) ? "true" : "false");
	} else {
		resp(readItems($email));
	}
} else {
	resp("fail", "authentication failed");
}

?>