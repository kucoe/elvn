<?php
require_once('params.php');

if($key) {
	if(auth($email, $key)) {
		resp(nextToken($email, $key));
	} else {
		resp('fail', 'authentication failed');
	}
} else {
	resp('fail', 'not enough params');
}

?>