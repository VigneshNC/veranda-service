package com.masterminds.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.masterminds.service.MessageService;

@RestController
@RequestMapping("/api/uploadthing")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UploadThingWebhookController {

	@Autowired
	private MessageService messageService;

	@GetMapping
	public ResponseEntity<?> handleDiscovery() {
		// The React SDK expects an ARRAY of route objects.
		// If it gets an Object {}, .find() will fail.
		String mockDiscovery = """
				[
				  {
				    "slug": "mediaUploader",
				    "config": {
				      "image": { "maxFileSize": "4MB" },
				      "video": { "maxFileSize": "16MB" }
				    }
				  }
				]
				""";

		return ResponseEntity.ok().header("Content-Type", "application/json").body(mockDiscovery);
	}

	@PostMapping
	public ResponseEntity<?> handleUploadRequest(@RequestBody Map<String, Object> clientRequest) {
		Map<String, Object> finalPayload = new HashMap<>();

		// Ensure this is exactly the List of Maps from the React request
		finalPayload.put("files", clientRequest.get("files"));

		finalPayload.put("callbackUrl", "https://veranda-service-production.up.railway.app/api/uploadthing/webhook");
		finalPayload.put("callbackSlug", "mediaUploader");

		// The 'routeConfig' must follow this exact hierarchy
		Map<String, Object> imageConfig = new HashMap<>();
		imageConfig.put("maxFileSize", "4MB");

		Map<String, Object> routeConfig = new HashMap<>();
		routeConfig.put("image", imageConfig);
//		 If you allow video too:
		Map<String, Object> videoConfig = new HashMap<>();
		videoConfig.put("maxFileSize", "16MB");
		routeConfig.put("video", videoConfig);

		finalPayload.put("routeConfig", routeConfig);

		// 3. Forward the complete request to UploadThing
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders utHeaders = new HttpHeaders();
		utHeaders.set("x-uploadthing-api-key",
				"sk_live_c8634138777b1d79aa7048f25c876b6ca0cdff2e1d731ec45fd0bc1857974a5f");
		utHeaders.setContentType(MediaType.APPLICATION_JSON);
		utHeaders.set("x-uploadthing-version", "6.10.0");

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(clientRequest, utHeaders);

		try {
			ResponseEntity<String> utResponse = restTemplate
					.postForEntity("https://api.uploadthing.com/v6/prepareUpload", entity, String.class);
			return ResponseEntity.ok(utResponse.getBody());
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			// Log the actual error body from UploadThing for easier debugging
			System.out.println("UploadThing Error: " + e.getResponseBodyAsString());
			return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
		}
	}

	@PostMapping("/webhook")
	public ResponseEntity<Void> handleWebhook(@RequestBody String payload) {
		// This payload contains the 'url' of the uploaded image/video
		System.out.println("Final Upload Details: " + payload);
		return ResponseEntity.ok().build();
	}
}