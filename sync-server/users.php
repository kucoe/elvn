<?php
require_once('params.php');

$id = userId($email);
$exists = userExists($id);
if(!$exists) {
	$id = 0;
}

if($key && $id == 0) {
	resp(newUser($email, $key));
} else if ($isDelete) {
	if(auth($email, $key)) {
		resp(deleteUser($email)); 
	} else {
		resp('fail', 'authentication failed');
	}
} else {
	resp($id);
}

?>