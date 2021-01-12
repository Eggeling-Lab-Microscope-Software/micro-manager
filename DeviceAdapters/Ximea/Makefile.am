
AM_CXXFLAGS = $(MMDEVAPI_CXXFLAGS) -I/opt/XIMEA/include -I../../3rdpartypublic/pugixml/pugixml-1.8/src
deviceadapter_LTLIBRARIES = libmmgr_dal_Ximea.la
libmmgr_dal_Ximea_la_SOURCES = XIMEACamera.cpp XIMEACamera.h xiApiPlus/xiAPIplus_core.cpp ../../3rdpartypublic/pugixml/pugixml-1.8/src/pugixml.cpp
libmmgr_dal_Ximea_la_LIBADD = $(MMDEVAPI_LIBADD) -lm3api
libmmgr_dal_Ximea_la_LDFLAGS = $(MMDEVAPI_LDFLAGS)