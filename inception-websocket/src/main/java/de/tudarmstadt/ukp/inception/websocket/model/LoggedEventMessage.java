/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.inception.websocket.model;


import java.util.Date;

public class LoggedEventMessage
{
    private String actorName;
    private String projectName;
    private String documentName;
    private Date creationDate;
    
    private String eventMsg;
    private String errorMsg;
    
    
    public LoggedEventMessage(String aActorName, String aProjectName, String aDocumentName,
            Date aCreationDate)
    {
        super();
        actorName = aActorName;
        projectName = aProjectName;
        documentName = aDocumentName;
        creationDate = aCreationDate;
    }

    public String getActorName()
    {
        return actorName;
    }
    public void setActorName(String aActorName)
    {
        actorName = aActorName;
    }
    public String getProjectName()
    {
        return projectName;
    }
    public void setProjectName(String aProjectName)
    {
        projectName = aProjectName;
    }
    public String getDocumentName()
    {
        return documentName;
    }
    public void setDocumentName(String aDocumentName)
    {
        documentName = aDocumentName;
    }
    public Date getCreationDate()
    {
        return creationDate;
    }
    public void setCreationDate(Date aCreationDate)
    {
        creationDate = aCreationDate;
    }
    public String getEventMsg()
    {
        return eventMsg;
    }
    public void setEventMsg(String aEventMsg)
    {
        eventMsg = aEventMsg;
    }
    public String getErrorMsg()
    {
        return errorMsg;
    }
    public void setErrorMsg(String aErrorMsg)
    {
        errorMsg = aErrorMsg;
    }

}
