<?php
require_once('lib.php');

$base = '/elvn';

$token = param('token', true);

$pass = param('pass');

if($pass) {
    $email = param('email');
    $key = hashPass($pass);
    $id = userId($email);
    if($id == 0) {
    	newUser($email, $key);
    }
    if(auth($email, $key)) {
        $token = nextToken($email, $key);
        header("Location: ".$base."/ui/".$token);
        exit();
    }
}

if($token) {
	$arr = readToken($token, false);
	if(!$arr) {
		header("Location: ".$base."/ui/");
        exit();
	}
	$email = $arr[0];
	$key = $arr[1];
}

$plan = param('plan', true);
$text = param('text');
$del = param('del');

if($token && $key) {
    if(auth($email, $key)) {
        $items = json_decode(readItems($email));
        if(!$items) {
            $items = array();
        }
    }
}

if($text && is_array($items)) {
    $p = $plan;
	if(!$p || $p == 'today') {
		$p = "work";
	}
	$item = (object) array('plan'=>array($p, 'today'), 'text'=>$text, 'id' => round(microtime(true) * 1000));
	array_push($items, $item);
	writeItems($email, json_encode($items));
}

if($del && is_array($items)) {
    $new_arr = array();
    foreach ($items as $value) {
        if($value->id != $del) {
            array_push($new_arr, $value);
        }
    }
	writeItems($email, json_encode($new_arr));
	$items = $new_arr;
}

?>
<html>
    <head>
        <title>elvn</title>
        <meta charset="utf8"/>
        <link href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body style="color:#555">
        <div class="well well-lg" style="width:80%; margin: auto; top:0; left:0; margin-top:25px;">
        <?php
            if (!$token) {
                echo '
                    <h2>welcome to elvn</h2>
                    <form method="post" class="form-horizontal" role="form">
                        <div class="form-group">
                            <div class="col-sm-5">
                                <input type="email" class="form-control" required=true name="email" placeholder="you@email"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-5">
                                <input type="password" class="form-control" required=true name="pass" placeholder="your******"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-5">
                                <input type="submit" class="btn btn-primary btn-lg btn-block" name="submit" value="Sign in"/>
                            </div>
                        </div>
                        <div>
                            do not have account?
                            no problem, enter your email, password and <b> login</b>
                        </div>
                    </form>';
            } else if(is_array($items)) {
                echo '<h2>welcome '.$email.'</h2>';
                $filtered = array();
                $tags = array();
                foreach ($items as $value) {
                    $i = $value->plan;
                    if(!is_array($i)) {
                        $i = array($i);
                    }
                    $tags = array_merge($tags, $i);
                    if(!$plan || in_array($plan, $i)) {
                        array_push($filtered, $value);
                    }
                }
                $tags = array_unique($tags);
                echo "<a href='".$base."/ui/".$token."'>all</a> | ";
                foreach ($tags as $tag) {
                    echo "<a href='".$base."/ui/".$token."/".$tag."'>".$tag."</a> | ";
                }
                echo "<hr/>";
                $i = 1;
                foreach ($filtered as $value) {
                    echo '<form method="post" class="form-inline"  role="form"><div class="form-group"><span>';
                    echo $i.". ".json_encode($value->plan).":".$value->text;
                    echo '</span><input type="hidden" name="del" value="'.$value->id.'"/>';
                    echo '</div><div class="form-group" style="margin-left:20px"><input type="submit" class="btn btn-danger btn-sm" name="submit" value="Remove"/></div></form><br />';
                    $i++;
                }
                echo '
                    <hr/>
                    <form method="post" class="form-inline" role="form">
                        <div class="form-group">
                            <input type="text" class="form-control" required=true name="text" placeholder="item text"/>
                        </div>
                        <div class="form-group">
                            <input type="submit" class="btn btn-primary" name="submit" value="Add"/>
                        </div>
                    </form>';
            }?>
        </div>
    </body>
</html>