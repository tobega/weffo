/*
 * Copyright (c) 2005 Torbjšrn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package net.homelinux.tobe.weffo;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Iterator;
import java.io.File;

/**
 * Utility class for doing the transform dance in Java
 */
public class Weffo {

    static Templates template;
    public static final String VERSION = "1.2";

    static Templates getTemplate() throws TransformerConfigurationException {
        if (template == null) {
            TransformerFactory tf = TransformerFactory.newInstance();
            InputStream is = Weffo.class.getResourceAsStream("/weffo.xsl");
            //Can't use StreamSource because default parser isn't namespace aware!
            try {
                XMLReader xr = XMLReaderFactory.createXMLReader();
                template = tf.newTemplates(new SAXSource(xr, new InputSource(is)));
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        return template;
    }

    /**
     * The simplest and most flexible way to create the output
     *
     * @param viewSource  the annotated view prototype
     * @param modelSource the dynamic data
     * @param viewResult  what you should send to the client
     * @throws TransformerException
     */
    public static void outputFromPrototype(Source viewSource, Source modelSource, Result viewResult) throws TransformerException {
        Transformer first = getTemplate().newTransformer();
        StringWriter generator = new StringWriter();
        first.transform(viewSource, new StreamResult(generator));
        TransformerFactory tf = TransformerFactory.newInstance();
        //Can't use StreamSource because default parser isn't namespace aware!
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            Transformer second = tf.newTransformer(new SAXSource(xr, new InputSource(new StringReader(generator.toString()))));
            second.transform(modelSource, viewResult);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sometimes you wish to pass in parameters that come from another source than the model.
     * The key in the map should have a toString method returning a String corresponding
     * to a parameter in the view declared with a weffo-param processing-instruction.
     * The value in the map is recommended to be a String, but the supported types of Object
     * depend on the transformer implementation.
     *
     * @param viewSource  the annotated view prototype
     * @param modelSource the dynamic data
     * @param viewResult  what you should send to the client
     * @param params   the parameters
     * @throws TransformerException
     */
    public static void outputFromPrototype(Source viewSource, Source modelSource, Result viewResult, Map params) throws TransformerException {
        Transformer first = getTemplate().newTransformer();
        StringWriter generator = new StringWriter();
        first.transform(viewSource, new StreamResult(generator));
        TransformerFactory tf = TransformerFactory.newInstance();
        //Can't use StreamSource because default parser isn't namespace aware!
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            Transformer second = tf.newTransformer(new SAXSource(xr, new InputSource(new StringReader(generator.toString()))));
            if (params != null) {
                for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
                    Map.Entry me = (Map.Entry) i.next();
                    second.setParameter(me.getKey().toString(), me.getValue());
                }
            }
            second.transform(modelSource, viewResult);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * To access other xml data than the model, the recommended approach is to pass in a URN as a parameter value (e.g. "urn:UserPrefs" )
     * and create a URIResolver implementation to return a Source with your other xml data.
     * When your URIResolver returns null for a requested resource, the transformer will proceed
     * and try and find it in a normal way (e.g. convert it to an URL).
     *
     * The data is then accessed in the view by "document($param)/XPathSelector".
     *
     * @param viewSource  the annotated view prototype
     * @param modelSource the dynamic data
     * @param viewResult  what you should send to the client
     * @param params   the parameters
     * @param uriResolver  your URIResolver implementation
     * @throws TransformerException
     */
    public static void outputFromPrototype(Source viewSource, Source modelSource, Result viewResult, Map params, URIResolver uriResolver) throws TransformerException {
        Transformer first = getTemplate().newTransformer();
        StringWriter generator = new StringWriter();
        first.transform(viewSource, new StreamResult(generator));
        TransformerFactory tf = TransformerFactory.newInstance();
        //Can't use StreamSource because default parser isn't namespace aware!
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            Transformer second = tf.newTransformer(new SAXSource(xr, new InputSource(new StringReader(generator.toString()))));
            if (params != null) {
                for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
                    Map.Entry me = (Map.Entry) i.next();
                    second.setParameter(me.getKey().toString(), me.getValue());
                }
            }
            if (uriResolver != null) second.setURIResolver(uriResolver);
            second.transform(modelSource, viewResult);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a cacheable, reusable template for generating a view
     *
     * @param viewSource the annotated view prototype
     * @return the generator template for the view
     * @throws TransformerException
     */
    public static Templates templateFromPrototype(Source viewSource) throws TransformerException {
        StringWriter generator = new StringWriter();
        transformFromPrototype(viewSource, new StreamResult(generator));
        TransformerFactory tf = TransformerFactory.newInstance();
        Templates generatorTemplate = null;
        //Can't use StreamSource because default parser isn't namespace aware!
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            generatorTemplate = tf.newTemplates(new SAXSource(xr, new InputSource(new StringReader(generator.toString()))));
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return generatorTemplate;
    }

    /**
     * Creates a transform for generating a view
     *
     * @param viewSource the annotated view prototype
     * @param transformResult the resulting transform for the view
     * @throws TransformerException
     */
    public static void transformFromPrototype(Source viewSource, Result transformResult) throws TransformerException {
        Transformer first = getTemplate().newTransformer();
        first.transform(viewSource, transformResult);
    }

    /**
     * the most efficient way: use a cached template
     *
     * @param generatorTemplate the cached generator for the view
     * @param modelSource       the dynamic data
     * @param viewResult        what you should send to the client
     * @throws TransformerException
     */
    public static void outputFromTemplate(Templates generatorTemplate, Source modelSource, Result viewResult) throws TransformerException {
        Transformer second = generatorTemplate.newTransformer();
        second.transform(modelSource, viewResult);
    }


    /**
     * Sometimes you wish to pass in parameters that come from another source than the model.
     * The key in the map should have a toString method returning a String corresponding
     * to a parameter in the view declared with a weffo-param processing-instruction.
     * The value in the map is recommended to be a String, but the supported types of Object
     * depend on the transformer implementation.
     *
     * @param generatorTemplate the cached generator for the view
     * @param modelSource the dynamic data
     * @param viewResult  what you should send to the client
     * @param params   the parameters
     * @throws TransformerException
     */
    public static void outputFromTemplate(Templates generatorTemplate, Source modelSource, Result viewResult, Map params) throws TransformerException {
        Transformer second = generatorTemplate.newTransformer();
        if (params != null) {
            for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
                Map.Entry me = (Map.Entry) i.next();
                second.setParameter(me.getKey().toString(), me.getValue());
            }
        }
        second.transform(modelSource, viewResult);
    }


    /**
     * To access other xml data than the model, the recommended approach is to pass in a URN as a parameter value (e.g. "urn:UserPrefs" )
     * and create a URIResolver implementation to return a Source with your other xml data.
     * When your URIResolver returns null for a requested resource, the transformer will proceed
     * and try and find it in a normal way (e.g. convert it to an URL).
     *
     * The data is then accessed in the view by "document($param)/XPathSelector".
     *
     * @param generatorTemplate the cached generator for the view
     * @param modelSource the dynamic data
     * @param viewResult  what you should send to the client
     * @param params   the parameters
     * @param uriResolver  your URIResolver implementation
     * @throws TransformerException
     */
    public static void outputFromTemplate(Templates generatorTemplate, Source modelSource, Result viewResult, Map params, URIResolver uriResolver) throws TransformerException {
        Transformer second = generatorTemplate.newTransformer();
        if (params != null) {
            for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
                Map.Entry me = (Map.Entry) i.next();
                second.setParameter(me.getKey().toString(), me.getValue());
            }
            if (uriResolver != null) second.setURIResolver(uriResolver);
        }
        second.transform(modelSource, viewResult);
    }
    
    public static void main(String[] args)  throws TransformerException {
        if(args.length != 2) {
            System.out.println("Usage: Weffo output-stylesheet-file view-template-file");
            System.exit(1);
        }
        transformFromPrototype(new StreamSource(new File(args[1])), new StreamResult(new File(args[0])));
    }
}
