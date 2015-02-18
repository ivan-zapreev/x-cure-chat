/**
 * X-Cure-Chat
 * Copyright (C) 2013  Dr. Ivan S. Zapreev
 * www: https://nl.linkedin.com/in/zapreevis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.#
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The user interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This is the base proxy UI class that allows for lazy loading of the site sections
 */
public abstract class SiteBodySectionContentProxy<SectionBodyType extends SiteBodySectionContent> extends SimplePanel implements SiteBodySectionContent {
	//The local method index counter for issuing the method ids
	private static int method_id_counter = 0;
	
	/**
	 * Allows to get a new method id
	 * @return the new method id
	 */
	protected static int getNewMethodId() {
		return method_id_counter++;
	}
	
	//The known method ids for the methods located in the SiteBodySectionContent interface
	protected final static int UNKNOWN_METHOD_ID 					= getNewMethodId();
	protected final static int updateTargetHistoryToken_METHOD_ID 	= getNewMethodId();
	protected final static int processHistoryToken_METHOD_ID 		= getNewMethodId();
	protected final static int setEnabled_METHOD_ID 				= getNewMethodId();
	protected final static int updateUIElements_METHOD_ID 			= getNewMethodId();
	protected final static int onAfterComponentIsAdded_METHOD_ID 	= getNewMethodId();
	protected final static int onBeforeComponentIsAdded_METHOD_ID 	= getNewMethodId();
	protected final static int onBeforeComponentIsRemoved_METHOD_ID = getNewMethodId();
	protected final static int setUserLoggedIn_METHOD_ID 			= getNewMethodId();
	protected final static int setUserLoggedOut_METHOD_ID 			= getNewMethodId();
	
	/**
	 * @author zapreevis
	 * Represents the queued method call.
	 * @param <ArgCont> the type of the argument(s) object
	 */
	public class QueueMethodCall<ArgCont> {
		//A known method id
		public int methodId = UNKNOWN_METHOD_ID;
		//the arguments if one argument then just the argument itself, 
		//otherwise some object storing arguments, if no arguments then null
		public ArgCont arguments = null;
	}
	
	//The list of currently queued method calls
	private final List<QueueMethodCall<?>> queuedMethodCalls = new ArrayList<QueueMethodCall<?>>();
	
	//The instance of the sections component, i.e. the actual loaded site section
	private SectionBodyType sectionBodyComponent = null;
	
	//The site section history prefix
	protected final String siteSectionPrefix; 
	
	/**
	 * The basic constructor provided with the site section url prefix
	 * @param siteSectionPrefix the history token site section prefix
	 */
	public SiteBodySectionContentProxy( final String siteSectionPrefix ) {
		super();
		
		//Store the data
		this.siteSectionPrefix = siteSectionPrefix;
	}
	
	/**
	 * This constructor will not be needed
	 */
	@SuppressWarnings("unused")
	private SiteBodySectionContentProxy(Element elem) { siteSectionPrefix = ""; }
	
	/**
	 * Allows to get the current instance of the site section body component
	 * @return the loaded instance of the site section body component, or null if it is not yet set/loaded
	 */
	protected SectionBodyType getSiteSectionBodyComponent() {
		return sectionBodyComponent;
	}
	
	/**
	 * Allows to set the loaded instance of the site section body component
	 * @param sectionBodyComponent the loaded instance of the site section body component, not null
	 */
	private void setSiteSectionBodyComponent( final SectionBodyType sectionBodyComponent ) {
		if( sectionBodyComponent instanceof Widget ) {
			this.sectionBodyComponent = sectionBodyComponent;
			this.add( (Widget) sectionBodyComponent );
		} else {
			Window.alert( "The site section body component " + sectionBodyComponent + " is not a Widget!" );
		}
	}
	
	/**
	 * Allows to remove the currently set instance of the site section body component
	 */
	protected void removeSiteSectionBodyComponent() {
		if( sectionBodyComponent != null ) {
			sectionBodyComponent = null;
			this.clear();
		}
	}
	
	/**
	 * Allows to load the actual site section component and then execute the queyed methods
	 */
	private void loadAndProcessMethodCallsQueue() {
		(new SplitLoad( true ) {
				@Override
				public void execute() {
					//Get the actual site section component if it is not there yet
					if( getSiteSectionBodyComponent() == null ) {
						setSiteSectionBodyComponent( loadSiteSectionBodyComponentInstance( siteSectionPrefix ) );
					}
					//Process currently queued method calls
					processMethodCallsQueue( getSiteSectionBodyComponent() );
				}
			}).loadAndExecute();
	}
	
	/**
	 * Allows to process all of the currently queued method calls.
	 * @param sectionComponent an instance of the actual site section body component, not null
	 */
	private void processMethodCallsQueue( final SectionBodyType sectionComponent ) {
		if( ! queuedMethodCalls.isEmpty() ) {
			Iterator<QueueMethodCall<?>> iterator = queuedMethodCalls.iterator();
			while( iterator.hasNext() ) {
				//Get the next call in the queue
				QueueMethodCall<?> queuedMethodCall = iterator.next();
				//Process the call
				boolean isOK = processQueuedMethodCall( sectionComponent, queuedMethodCall );
				if( ! isOK ) {
					//Should not be happening in the production code
					Window.alert( "Could not process the queued method with id " + queuedMethodCall.methodId );
				}
				//Remove the processed call
				iterator.remove();
			}
		}
	}
	
	/**
	 * Allows to queue a new method call
	 * @param methodCall the method call that will be queued
	 */
	protected void queueMethodCall( QueueMethodCall<?> methodCall ) {
		//Put the new method call into the queue
		queuedMethodCalls.add( methodCall );
		//Load the site section body component and execute the queued methods
		loadAndProcessMethodCallsQueue();
	}
	
	/**
	 * Allows to process the queued method call. Should be overridden 
	 * to allow for additional interface methods of the subclasses.
	 * @param sectionComponent an instance of the actual site section body component, not null
	 * @param queuedMethodCall the queued method call that will have to be executed, not null
	 * @return true if the method was successfully processed (found and executed), otherwise false
	 */
	protected boolean processQueuedMethodCall( final SectionBodyType sectionComponent, final QueueMethodCall<?> queuedMethodCall ) {
		//First we assume that the method will be found and executed
		boolean isOK = true;
		//Then we try to map the method and execute it
		if( queuedMethodCall.methodId == updateTargetHistoryToken_METHOD_ID ) {
			sectionComponent.updateTargetHistoryToken( (Anchor) queuedMethodCall.arguments );
		} else {
			if( queuedMethodCall.methodId == processHistoryToken_METHOD_ID ) {
				sectionComponent.processHistoryToken( (String) queuedMethodCall.arguments );
			} else {
				if( queuedMethodCall.methodId == setEnabled_METHOD_ID ) {
					sectionComponent.setEnabled( (Boolean) queuedMethodCall.arguments );
				} else {
					if( queuedMethodCall.methodId == updateUIElements_METHOD_ID ) {
						sectionComponent.updateUIElements();
					} else {
						if( queuedMethodCall.methodId == onAfterComponentIsAdded_METHOD_ID ) {
							sectionComponent.onAfterComponentIsAdded();
						} else {
							if( queuedMethodCall.methodId == onBeforeComponentIsAdded_METHOD_ID ) {
								sectionComponent.onBeforeComponentIsAdded();
							} else {
								if( queuedMethodCall.methodId == onBeforeComponentIsRemoved_METHOD_ID ) {
									sectionComponent.onBeforeComponentIsRemoved();
								} else {
									if( queuedMethodCall.methodId == setUserLoggedIn_METHOD_ID ) {
										sectionComponent.setUserLoggedIn();
									} else {
										if( queuedMethodCall.methodId == setUserLoggedOut_METHOD_ID ) {
											sectionComponent.setUserLoggedOut();
										} else {
											//The method was not found, make a note of this fact
											isOK = false;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		return isOK;
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodySectionContent#updateTargetHistoryToken(com.google.gwt.user.client.ui.Anchor)
	 */
	@Override
	final public void updateTargetHistoryToken(Anchor anchorLink) {
		//Update the history token only if the site section was loaded
		if( getSiteSectionBodyComponent() != null ) {
			QueueMethodCall<Anchor> methodCall = new QueueMethodCall<Anchor>();
			methodCall.methodId = updateTargetHistoryToken_METHOD_ID;
			methodCall.arguments = anchorLink;
			queueMethodCall( methodCall );
			Log.debug("Invoking method updateTargetHistoryToken from " + siteSectionPrefix );
		} else {
			//If not then just put a dummy history token
			anchorLink.setHref( CommonResourcesContainer.URI_HASH_SYMBOL + siteSectionPrefix );
		}
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodySectionContent#processHistoryToken(java.lang.String)
	 */
	@Override
	final public void processHistoryToken(String historyToken) {
		QueueMethodCall<String> methodCall = new QueueMethodCall<String>();
		methodCall.methodId = processHistoryToken_METHOD_ID;
		methodCall.arguments = historyToken;
		queueMethodCall( methodCall );
		Log.debug("Invoking method processHistoryToken from " + siteSectionPrefix );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodySectionContent#setEnabled(boolean)
	 */
	@Override
	final public void setEnabled(boolean enabled) {
		QueueMethodCall<Boolean> methodCall = new QueueMethodCall<Boolean>();
		methodCall.methodId = setEnabled_METHOD_ID;
		methodCall.arguments = enabled;
		queueMethodCall( methodCall );
		Log.debug("Invoking method setEnabled from " + siteSectionPrefix );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodySectionContent#updateUIElements()
	 */
	@Override
	final public void updateUIElements() {
		//Only do this if the component is already loaded, otherwise if
		//it is not there then there is no need to perform this action
		if( getSiteSectionBodyComponent() != null ) {
			QueueMethodCall<Void> methodCall = new QueueMethodCall<Void>();
			methodCall.methodId = updateUIElements_METHOD_ID;
			queueMethodCall( methodCall );
			Log.debug("Invoking method updateUIElements from " + siteSectionPrefix );
		}
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onAfterComponentIsAdded()
	 */
	@Override
	final public void onAfterComponentIsAdded() {
		QueueMethodCall<Void> methodCall = new QueueMethodCall<Void>();
		methodCall.methodId = onAfterComponentIsAdded_METHOD_ID;
		queueMethodCall( methodCall );
		Log.debug("Invoking method onAfterComponentIsAdded from " + siteSectionPrefix );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onBeforeComponentIsAdded()
	 */
	@Override
	final public void onBeforeComponentIsAdded() {
		QueueMethodCall<Void> methodCall = new QueueMethodCall<Void>();
		methodCall.methodId = onBeforeComponentIsAdded_METHOD_ID;
		queueMethodCall( methodCall );
		Log.debug("Invoking method onBeforeComponentIsAdded from " + siteSectionPrefix );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onBeforeComponentIsRemoved()
	 */
	@Override
	final public void onBeforeComponentIsRemoved() {
		//Only do this if the component is already loaded, otherwise if
		//it is not there then there is no need to perform this action
		if( getSiteSectionBodyComponent() != null ) {
			QueueMethodCall<Void> methodCall = new QueueMethodCall<Void>();
			methodCall.methodId = onBeforeComponentIsRemoved_METHOD_ID;
			queueMethodCall( methodCall );
			Log.debug("Invoking method onBeforeComponentIsRemoved from " + siteSectionPrefix );
		}
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setUserLoggedIn()
	 */
	@Override
	final public void setUserLoggedIn() {
		QueueMethodCall<Void> methodCall = new QueueMethodCall<Void>();
		methodCall.methodId = setUserLoggedIn_METHOD_ID;
		queueMethodCall( methodCall );
		Log.debug("Invoking method setUserLoggedIn from " + siteSectionPrefix );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setUserLoggedOut()
	 */
	@Override
	final public void setUserLoggedOut() {
		//Only do this if the component is already loaded, otherwise if
		//it is not there then there is no need to perform this action
		if( getSiteSectionBodyComponent() != null ) {
			QueueMethodCall<Void> methodCall = new QueueMethodCall<Void>();
			methodCall.methodId = setUserLoggedOut_METHOD_ID;
			queueMethodCall( methodCall );
			Log.debug("Invoking method setUserLoggedOut from " + siteSectionPrefix );
		}
	}
	
	/**
	 * Allows to get an new instance of the site section body component
	 * @param siteSectionPrefix the history token site section prefix
	 * @return an new instance of the site section body component
	 */
	public abstract SectionBodyType loadSiteSectionBodyComponentInstance( final String siteSectionPrefix );

}
