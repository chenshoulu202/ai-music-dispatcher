import websocket
import json
import time

# WebSocket server URL
WEBSOCKET_URL = "ws://localhost:8080/ws/dy"

# Sample WebcastChatMessage payload
# You can modify this payload as needed for testing different songs or users
CHAT_MESSAGE_PAYLOAD = {
    "id": str(int(time.time() * 1000)), # Unique ID for each message
    "method": "WebcastChatMessage",
    "user": {
        "id": "MS4wLjABAAAAA1j9svveTKS5Aa2p-LUpBN1tFGR3l1NQzi0UlkHDGDU", # Example user ID
        "name": "囧囧有神璐璐", # Example user name
        "gender": 1,
        "avatar": "https://example.com/avatar.jpg"
    },
    "content": "点歌 奢香夫人", # The song request content
    "room": {
        "audienceCount": "100",
        "likeCount": "500",
        "followCount": "1000"
    }
}

def on_message(ws, message):
    print(f"Received from server: {message}")

def on_error(ws, error):
    print(f"Error: {error}")

def on_close(ws, close_status_code, close_msg):
    print("### Connection closed ###")

def on_open(ws):
    print("### Connection opened ###")
    print("Sending WebcastChatMessage...")
    ws.send(json.dumps(CHAT_MESSAGE_PAYLOAD))
    print("Message sent. Closing connection in 2 seconds...")
    time.sleep(2) # Give server time to process
    ws.close()

if __name__ == "__main__":
    # websocket.enableTrace(True) # Uncomment for detailed WebSocket logging
    ws = websocket.WebSocketApp(WEBSOCKET_URL,
                              on_open=on_open,
                              on_message=on_message,
                              on_error=on_error,
                              on_close=on_close)

    ws.run_forever()
