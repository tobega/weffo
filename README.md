![image info](weffo.png)
# Introduction and complete manual
## Name and rationale
The name "Weffo" (pronounced sort of like "wherefore") is an anglicisation of the expression "Voffo" from Astrid Lindgren's book "Ronja rövardotter" ("Ronja the robber's daughter")

Little forest creatures use this expression when looking at humans and not understanding at all what they are doing.

Exactly that sentiment is what I would like to express when looking at web frameworks.

## Principles
- MVC pattern (or perhaps "Triangle" pattern from "Bitter Java")
- View should be creatable by a view designer with no coding skills
- View should be "demoable" in a normal browser, without any coding being done, even with view transitions prototypable
- Mechanism should preferably be language agnostic
- SOA backend processing
- Parallelizable development

## Download
This software is available under the LGPL license

Download the [java utility](https://github.com/tobega/weffo/blob/main/weffo.jar) or just the [xsl transform](https://github.com/tobega/weffo/blob/main/weffo.xsl)

### Utility and example implementations
- Java utility - [javadocs](https://github.com/tobega/weffo/blob/main/java/index.html) - included in the jar

## Manual
### Model
Create a data model (or several) and an xml representation of it.
### Prototype View
Create views in the chosen xml presentation format, e.g. xhtml.

For a demoable prototype, just put in your demo data in the document and represent transitions (form submits, links, etc.) by
linking to the next view document.
### Commands and Services (ignore if you're just creating a stylesheet)
Define the different "commands" that are to be processed and map these to back-end processing logic ("services").
In a simple app, a service could just be a method call.

Make sure each command returns an xml representation consistent with the model
### Controller (ignore if you're just creating a stylesheet)
Create controller logic that maps a web request to a command.

Create logic to connect the command result to a view. This is done by first applying the weffo.xsl xsl transform to the view,
and then applying the thus generated xsl transform to the command result, and the final result is streamed out.

### Change the prototype view to production view
In the view prototype, replace the static transitions by the correct web requests corresponding to the command and annotate
the xml tags that are to receive dynamic data. Remove or annotate tags that were created only for demo purposes:

It may be a good idea to study the [XPath specification](http://www.w3.org/TR/xpath) and keep it handy for reference.
However, for most purposes it is enough to read [this section](http://www.w3.org/TR/xpath#path-abbrev") of it.

- On the root tag (or a sufficiently high-level tag, e.g. Nvu accepts it on "body" but not on "html"), define xmlns:w="http://tobe.homelinux.net/weffo".
	Also define all namespaces you reference in the XPath selectors mentioned below.
- On each dynamically repeatable tag, add the attribute w:foreach="*XPath selector*", where the XPath selector selects the applicable data from the model.
- On each tag whose content is to be replaced by dynamic data, add either:
	- the attribute w:text="*XPath selector*", which will replace the contents of the tag with the text contents of the selected model nodes
	- or the attribute w:content="*XPath selector*", which will replace the contents of the tag with the actual selected model nodes (including mark-up). If you only want the content of the selected model nodes, append "/node()" to the selector.

	These attributes will cause the content (put in for demo purposes) of the view element to be replaced by the content of the selected model nodes.
	For marked-up content, use w:content, while if you want just the concatenated text content  without tags from the model node, use w:text
- On each tag that needs to have attributes set by dynamic data, add the attribute w:attributes="*attribute name*,*XPath selector*[;...]" (with a semi-colon between following name-selector pairs)
- On each tag that is to be ignored in the production view, add the attribute w:demo="demo" (actually, the value of the w:demo 		attribute is not important, it just needs to have a value).
	You needn't put this tag on content that will be replaced by a w:text or w:content attribute</li>

#### Notes
XPath selectors should normally be absolute (the context node is the root, "/")

In a subtree with w:foreach set (including the element that has the w:foreach), XPath selectors can be given
relative to the node being looped on (the context node is the current repetition of the node selected in the closest outer w:foreach)

The w:demo attribute takes precedence over the other weffo attributes.

### Additional features
- Parameters (extra dynamic data beyond the model): You can add transform parameters containing data not in the model.
  Use the processing-instruction
	"<?weffo-param *parameterName*=*defaultValue*?>" before the root node of your view to specify parameters. The parameter can be referenced in XPath selectors by "$parameterName".
	If you specify a URI to an XML document, you can access the document nodes by "document($parameterName)/*XPath selector*"
- Generate processing-instructions (e.g. xml-stylesheet declarations): Use the processing-instruction "<?weffo-pi *pi-name* 			*XPath selector*?>" to create a processing-instruction with dynamically generated content.

#### Notes
The type of objects that may be passed as parameters depend upon the transformer implementation. Strings are a safe bet, but some transformers handle DOM nodes.

In java, to pass a DOM node/document as a parameter, it is recommended to pass in a URN as the parameter and create URIResolver for the transformer that will return a Source for the document. See the [javadoc](https://github.com/tobega/weffo/blob/main/java/net/homelinux/tobe/weffo/Weffo.html#outputFromPrototype(javax.xml.transform.Source,%20javax.xml.transform.Source,%20javax.xml.transform.Result,%20java.util.Map,%20javax.xml.transform.URIResolver))

### Tips and tricks
Use w:foreach as a conditional test. If the XPath selector in the w:foreach resolves to an empty set, the view node with the w:foreach on it will not be generated.

Use w:foreach to change the context node. It may be more convenient to change the model context node with a w:foreach on a view node and then just use relative XPath selectors inside that view node, even if you know that the w:foreach will only give one occurence.

Tip from Peder Persson: Modern browsers can use xsl transforms as stylesheets. If you create the generator transform in advance, you can publish it as static content.
Then just send your model data to the user's browser with an xml-stylesheet processing-instruction on it,
e.g. `<?xml-stylesheet href="myGenerator.xsl" type="text/xsl"?>` The efficiencies are two-fold: (1) the browser can cache the generator transform and (2) the user's computer is utilized for the second transform.
