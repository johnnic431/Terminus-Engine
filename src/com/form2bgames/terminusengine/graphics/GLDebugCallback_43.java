package com.form2bgames.terminusengine.graphics;

import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL43.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GLDebugCallback_43 extends GLDebugMessageCallback {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void invoke(int source, int type, int id, int severity, int len, long msg, long udata) {
		if (id == 131185) // || id == 131186 || id == 131218
			return;
		String _source, _type, _severity;
		switch (source) {
		case GL_DEBUG_SOURCE_API:
			_source = "API";
			break;

		case GL_DEBUG_SOURCE_WINDOW_SYSTEM:
			_source = "WINDOW SYSTEM";
			break;

		case GL_DEBUG_SOURCE_SHADER_COMPILER:
			_source = "SHADER COMPILER";
			break;

		case GL_DEBUG_SOURCE_THIRD_PARTY:
			_source = "THIRD PARTY";
			break;

		case GL_DEBUG_SOURCE_APPLICATION:
			_source = "APPLICATION";
			break;

		case GL_DEBUG_SOURCE_OTHER:
			_source = "UNKNOWN";
			break;

		default:
			_source = "UNKNOWN";
			break;
		}

		switch (type) {
		case GL_DEBUG_TYPE_ERROR:
			_type = "ERROR";
			break;

		case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
			_type = "DEPRECATED BEHAVIOR";
			break;

		case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
			_type = "UDEFINED BEHAVIOR";
			break;

		case GL_DEBUG_TYPE_PORTABILITY:
			_type = "PORTABILITY";
			break;

		case GL_DEBUG_TYPE_PERFORMANCE:
			_type = "PERFORMANCE";
			break;

		case GL_DEBUG_TYPE_OTHER:
			_type = "OTHER";
			break;

		case GL_DEBUG_TYPE_MARKER:
			_type = "MARKER";
			break;

		default:
			_type = "UNKNOWN";
			break;
		}

		switch (severity) {
		case GL_DEBUG_SEVERITY_HIGH:
			_severity = "HIGH";
			break;

		case GL_DEBUG_SEVERITY_MEDIUM:
			_severity = "MEDIUM";
			break;

		case GL_DEBUG_SEVERITY_LOW:
			_severity = "LOW";
			break;

		case GL_DEBUG_SEVERITY_NOTIFICATION:
			_severity = "NOTIFICATION";
			break;

		default:
			_severity = "UNKNOWN";
			break;
		}

		logger.error("ID {}: {} of {} severity, raised from {}: {}", id, _type, _severity, _source,
				MemoryUtil.memASCII(msg));
		logger.info("");
	}
}