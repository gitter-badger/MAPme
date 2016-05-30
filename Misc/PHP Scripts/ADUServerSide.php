<?php

// upload images

if (isset($_FILES['images'])) {
	foreach($_FILES['images']['error'] as $key => $error) {
		if ($error == UPLOAD_ERR_NO_FILE) { //required for multi image form inputs
			continue;
		}
		else {
			if ($error == UPLOAD_ERR_OK) {
				if ((in_array($_FILES['images']['type'][$key], $image_types) && $imagecount < $imagelimit)) {
					if ($_FILES['images']['size'][$key] > 0 && $_FILES['images']['size'][$key] < 5000000) {

						// create the file name

						$name_array = explode('.', $_FILES['images']['name'][$key]); //to get the extension
						$file_type = strtolower(end($name_array)); //
						$fname = $uploadfolder . sprintf($file_format, $new_vm_number, $imagecount + 1, $file_type);

						// try to move the file

						if (move_uploaded_file($_FILES['images']['tmp_name'][$key], $fname)) { //moves the file and checks if successful
							$uploadcount++;
							$imagecount++;
						}
						else {

							// delete the record

							$sql = 'DELETE FROM ' . $dbname . '.' . $uploadtable . ' WHERE vm_number = ' . $new_vm_number;
							$stmt = $db->prepare($sql);
							$stmt->execute();
							$db = null;

							// remove any files already transfered

							array_map('unlink', glob($uploadfolder . $new_vm_number . '*.*'));

							// exit

							error($meta, 'sound file transfer failed');
						}
					}
				}
			}
			else {

				// delete the record

				$sql = 'DELETE FROM ' . $dbname . '.' . $uploadtable . ' WHERE vm_number = ' . $new_vm_number;
				$stmt = $db->prepare($sql);
				$stmt->execute();
				$db = null;

				// remove any files already transfered

				array_map('unlink', glob($uploadfolder . $new_vm_number . '*.*'));

				// exit

				error($meta, 'image file upload error');
			}
		}
	}
}

// upload soundbytes

if (isset($_FILES['sound']) && in_array($data->project_name, $soundbyteprojects)) {
	if ($_FILES['sound']['error'] == UPLOAD_ERR_OK) {
		if (in_array($_FILES['sound']['type'], $sound_types) && $soundbytecount < $soundbytelimit) {
			if ($_FILES['sound']['size'] > 0 && $_FILES['sound']['size'] < 5000000) {

				// create the file name

				$name_array = explode('.', $_FILES['sound']['name']); //to get the extension
				$file_type = strtolower(end($name_array)); //
				$fname = $uploadfolder . sprintf($file_format, $new_vm_number, $soundbytecount + 1, $file_type);

				// try to move the file

				if (move_uploaded_file($_FILES['sound']['tmp_name'], $fname)) { //moves the file and checks if successful
					$uploadcount++;
					$soundbytecount++;
				}
				else {

					// delete the record

					$sql = 'DELETE FROM ' . $dbname . '.' . $uploadtable . ' WHERE vm_number = ' . $new_vm_number;
					$stmt = $db->prepare($sql);
					$stmt->execute();
					$db = null;

					// remove any files already transfered

					array_map('unlink', glob($uploadfolder . $new_vm_number . '*.*'));

					// exit

					error($meta, 'sound file transfer failed');
				}
			}
		}
	}
	else {

		// delete the record

		$sql = 'DELETE FROM ' . $dbname . '.' . $uploadtable . ' WHERE vm_number = ' . $new_vm_number;
		$stmt = $db->prepare($sql);
		$stmt->execute();
		$db = null;

		// remove any files already transfered

		array_map('unlink', glob($uploadfolder . $new_vm_number . '*.*'));

		// exit

		error($meta, 'sound file upload error');
	}
}

// add the number of uploads to the data object

$data->vm_photos = $imagecount; //the number of images uploaded

if ($data->vm_photos == 0 && isset($data->image_url)) { //if image
	$data->vm_photos = count(explode(";", $data->image_url));
}

$data->sound_recoding = $soundbytecount;
$meta->total_file_uploads = $uploadcount;
$dt_filetransfer_end = microtime_float();
$performance->filetransfer = $dt_filetransfer_end - $dt_filetranser_init;

// delete the record if there are no suitable files for vmupload records

if ($dbname == 'vmupload' && $uploadcount == 0) {
	$dt_deleterecord_init = microtime_float();
	$sql = 'DELETE FROM ' . $dbname . '.' . $uploadtable . ' WHERE vm_number = ' . $new_vm_number;
	$stmt = $db->prepare($sql);
	$stmt->execute();
	$db = null;
	$dt_deleterecord_end = microtime_float();
	$performance->delete_record = $dt_deleterecord_end - $dt_deleterecord_init;
	$dt_end = microtime_float();
	$performance->total = $dt_end - $dt_init;

	// $meta->performance = $performance;

	error($meta, 'no suitable files attached');
}
