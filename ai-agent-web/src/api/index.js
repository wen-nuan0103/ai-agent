import axios from 'axios'

const apiClient = axios.create({
    baseURL: 'http://localhost:8123/api',
    timeout: 60000,
    headers: {
        'Content-Type': 'application/json'
    }
})

/**
 * Create an SSE connection for AI Love Chat
 * @param {string} userMessage - The user's message
 * @param {string} chatId - The chat session ID
 * @param {function} onMessage - Callback on each SSE message
 * @param {function} onError - Callback on error
 * @param {function} onComplete - Callback when stream ends
 * @returns {EventSource} The EventSource instance
 */
export function connectLoveChatSSE(userMessage, chatId, onMessage, onError, onComplete) {
    const params = new URLSearchParams({ userMessage, chatId })
    const url = `http://localhost:8123/api/ai/love/chat/sse?${params.toString()}`
    const eventSource = new EventSource(url)

    eventSource.onmessage = (event) => {
        if (onMessage) onMessage(event.data)
    }

    eventSource.onerror = (error) => {
        eventSource.close()
        if (onError) onError(error)
        if (onComplete) onComplete()
    }

    return eventSource
}

/**
 * Create an SSE connection for AI Manus Chat
 * @param {string} userMessage - The user's message
 * @param {function} onMessage - Callback on each SSE message
 * @param {function} onError - Callback on error
 * @param {function} onComplete - Callback when stream ends
 * @returns {EventSource} The EventSource instance
 */
export function connectManusChatSSE(userMessage, onMessage, onError, onComplete) {
    const params = new URLSearchParams({ userMessage })
    const url = `http://localhost:8123/api/ai/manus/chat?${params.toString()}`
    const eventSource = new EventSource(url)

    eventSource.onmessage = (event) => {
        if (onMessage) onMessage(event.data)
    }

    eventSource.onerror = (error) => {
        eventSource.close()
        if (onError) onError(error)
        if (onComplete) onComplete()
    }

    return eventSource
}

/**
 * Trigger file download via backend API
 * @param {string} fileName - The name of the file
 */
export async function downloadFile(fileName) {
    try {
        const response = await apiClient.get('/file/download', {
            params: { fileName },
            responseType: 'blob' // Important for handling binary data
        })

        // Create blob link to download
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
    } catch (error) {
        console.error('File download failed:', error)
        throw error
    }
}

export default apiClient
