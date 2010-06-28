package com.aptana.editor.js.parsing.ast;

import java.util.LinkedList;
import java.util.List;

import com.aptana.editor.js.contentassist.LocationType;
import com.aptana.editor.js.sdoc.model.DocumentationBlock;
import com.aptana.editor.js.sdoc.model.Tag;
import com.aptana.editor.js.sdoc.model.TagType;
import com.aptana.editor.js.sdoc.model.TagWithTypes;
import com.aptana.editor.js.sdoc.model.Type;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseNodeAttribute;
import com.aptana.parsing.ast.ParseNode;
import com.aptana.parsing.ast.ParseNodeAttribute;

public class JSFunctionNode extends JSNode
{
	private List<String> fReturnTypes;

	/**
	 * JSFunctionNode
	 * 
	 * @param children
	 * @param start
	 * @param end
	 */
	public JSFunctionNode(int start, int end, JSNode... children)
	{
		super(JSNodeTypes.FUNCTION, start, end, children);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.parsing.ast.JSNode#accept(com.aptana.editor.js.parsing.ast.JSTreeWalker)
	 */
	@Override
	public void accept(JSTreeWalker walker)
	{
		walker.visit(this);
	}

	/**
	 * getArgs
	 * 
	 * @return
	 */
	public IParseNode[] getArgs()
	{
		IParseNode[] result = NO_CHILDREN;
		IParseNode argsNode = this.getChild(1);

		if (argsNode != null && argsNode.getNodeType() == JSNodeTypes.PARAMETERS)
		{
			result = argsNode.getChildren();
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.parsing.ast.ParseBaseNode#getAttributes()
	 */
	public IParseNodeAttribute[] getAttributes()
	{
		String name = getName();

		if (name != null && name.length() > 0)
		{
			// TODO: possibly cache this
			return new IParseNodeAttribute[] { new ParseNodeAttribute(this, "name", name) //$NON-NLS-1$
			};
		}
		else
		{
			return ParseNode.NO_ATTRIBUTES;
		}
	}

	/**
	 * getBody
	 * 
	 * @return
	 */
	public IParseNode getBody()
	{
		return this.getChild(2);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.parsing.ast.JSNode#getLocationType(int)
	 */
	@Override
	LocationType getLocationType(int offset)
	{
		LocationType result = LocationType.UNKNOWN;

		if (this.contains(offset))
		{
			IParseNode body = this.getBody();

			if (body.contains(offset))
			{
				result = ((JSNode) body).getLocationType(offset);
			}
			else
			{
				result = LocationType.NONE;
			}
		}

		return result;
	}

	/**
	 * getName
	 * 
	 * @return
	 */
	public String getName()
	{
		return getChild(0).getText();
	}

	/**
	 * getReturnTypes
	 * 
	 * @return
	 */
	public List<String> getReturnTypes()
	{
		if (fReturnTypes == null)
		{
			fReturnTypes = new LinkedList<String>();
			DocumentationBlock docs = this.getDocumentation();

			if (docs != null && docs.hasTags())
			{
				for (Tag tag : docs.getTags())
				{
					if (tag.getType() == TagType.RETURN)
					{
						TagWithTypes tagWithTypes = (TagWithTypes) tag;

						for (Type type : tagWithTypes.getTypes())
						{
							fReturnTypes.add(type.getName());
						}
					}
				}
			}
		}

		return fReturnTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.parsing.ast.ParseBaseNode#getText()
	 */
	@Override
	public String getText()
	{
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.parsing.ast.JSNode#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder text = new StringBuilder();
		text.append("function "); //$NON-NLS-1$
		String name = getName();
		if (name.length() > 0)
		{
			text.append(name).append(" "); //$NON-NLS-1$
		}
		text.append(getChild(1)).append(" ").append(getChild(2)); //$NON-NLS-1$

		this.appendSemicolon(text);

		return text.toString();
	}
}
