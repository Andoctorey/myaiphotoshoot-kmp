export async function resizeImageToWidth(
  inputBytes,
  targetWidth,
  mimeType = 'image/jpeg'
) {
  console.log("resizeImageToWidth")
  // 1) Convert the incoming Uint8Array to a Blob with flexible MIME
  const blob = new Blob([inputBytes], { type: mimeType });
  const url = URL.createObjectURL(blob);

  // 2) Load an Image
  const img = new Image();
  img.src = url;

  // Wait for load (fallback to onload if decode not supported or fails)
  try {
    await img.decode();
  } catch (err) {
    // Fallback approach
    await new Promise((resolve, reject) => {
      img.onload = resolve;
      img.onerror = reject;
    });
  }

  // Clean up
  URL.revokeObjectURL(url);

  // 3) Calculate scaled height
  const aspect = img.height / img.width;
  const scaledHeight = Math.round(targetWidth * aspect);

  // 4) Create canvas and draw
  const canvas = document.createElement('canvas');
  canvas.width = targetWidth;
  canvas.height = scaledHeight;
  const ctx = canvas.getContext('2d');
  ctx.imageSmoothingEnabled = true;
  ctx.imageSmoothingQuality = 'high';
  ctx.drawImage(img, 0, 0, targetWidth, scaledHeight);

  // 5) Extract as data URL (always convert to JPEG for consistency)
  const dataUrl = canvas.toDataURL('image/jpeg', 1.0);
  const base64 = dataUrl.split(',')[1];

  // 6) Convert Base64 -> Uint8Array
  const binary = atob(base64);
  const len = binary.length;
  const outputBytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    outputBytes[i] = binary.charCodeAt(i);
  }

  return outputBytes;
}