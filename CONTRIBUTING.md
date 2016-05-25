#How to Contribute
A great way to get involved in the HAT project is to contribute to the existing stacks of the HAT. The HAT Project team uses Github for its source code management. The current HAT Project development tree is at https://github.com/Hub-of-all-Things; 

##The HAT Project
HAT Project is an umbrella project that contains many sub projects. A project list of HAT project is available here (https://github.com/Hub-of-all-Things/HAT/blob/master/README.md). Below are some elements you’re likely to come across in HAT project on GitHub.

##The HAT Community
The HAT project has been supported by the HAT community since its inception back in 2013 as a research project. The HAT community is made up of users in different (formal or informal) roles:
* Owner - the organization that created the project. The HAT project was founded in 2013 by a HAT research consortium containing 6 UK Universities - Warwick (lead partner), Cambridge, Nottingham, Surrey, UWE, and Edinborough. The [HAT Community Foundatoin](http://hatcommunity.org/) took on the ownership, as well as the maintainers role after HAT project finished in 2015. 
* Maintainers - development community organised by the [HAT Community Foundatoin](http://hatcommunity.org/), primarily doing the work on HAT project and driving the direction. The maintainer have write access to the repository.
* Contributors is everyone who has had a pull request merged into a project.
* Community Members are the users who often use and care deeply about the project and are active in discussions for features and pull requests.

##The Docs
The what’s-what of common files in projects.
###Readme
All projects include a README.md file. The readme provides a lay of the land for a project with details on how to use, build and sometimes contribute to a project.
###License
A LICENSE file, well, is the license for a specific project. An open source project’s license informs users what they can and can’t do (e.g., use, modify, redistribute), and contributors, what they are allowing others to do. Different sub projects under the HAT project are licensed differently, so please refer to the LICENSE file under each project.
###Documentation and Wikis
HAT project goes beyond a readme to give instructions for how people can use the project. In such cases you’ll often find a link to files or folders named ‘docs’ in the repository.
Alternatively, the repository may instead use the different system for wiki to break down documentation. Please refer to the docs file of each project for more details.

##Contributing to HAT Project
This sections details the specifics about how HAT project’s maintainer would like to see patches or features contributed. This can include what tests to write, code syntax style or areas to focus on for patches.
###Create an Issue
If you find a bug in a project you’re using (and you don’t know how to fix it), have trouble following the documentation or have a question about the project – create an issue! There’s nothing to it and whatever issue you’re having, you’re likely not the only one, so others will find your issue helpful, too. For more information on how issues work, check out our Issues guide.
####Issues Pro Tips
Check existing issues for your issue. Duplicating an issue is slower for both parties so search through open and closed issues to see if what you’re running into has been addressed already.
Be clear about what your problem is: what was the expected outcome, what happened instead? Detail how someone else can recreate the problem.
Link to demos recreating the problem on things like JSFiddle or CodePen.
Include system details like what the browser, library or operating system you’re using and its version.
Paste error output or logs in your issue or in a Gist. If pasting them in the issue, wrap it in three backticks: ` ``` ` so that it renders nicely.
###Pull Request
If you’re able to patch the bug or add the feature yourself – fantastic, make a pull request with the code! Be sure you’ve read any documents on contributing, and understand the license. The project does not require community members to sign contribution or committer agreements.
#### Developer Certificate Of Origin
We do require contributors to sign contributions using the sign-off feature of the [Developer Certificate Of Origin](https://github.com/Hub-of-all-Things/HAT/blob/master/DCO). Please include a signed-off-by line as part of your commit comments when you submit a pull request. Here is an example Signed-off-by line, that indicates the submitter accepts the DCO: 

```Signed-off-by: John Doe <john.doe@hisdomain.com>```

#### Header for new files
If you are creating any new file, please attach the following notices to the program to the start of each source file. 

```Copyright (C) <year> <name of author|name of company>```
    
```SPDX-License-Identifier: AGPL-3.0```

```This file is part of the Hub of All Things (HAT) project. HAT is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3 of the License. HAT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.```
    
```This is a file submitted under the HAT Project - https://github.com/Hub-of-all-Things```

###Pull Request Pro Tips
* Fork the repository and clone it locally. Connect your local to the original ‘upstream’ repository by adding it as a remote. Pull in changes from ‘upstream’ often so that you stay up to date so that when you submit your pull request, merge conflicts will be less likely. See more detailed instructionshere.
* Create a branch for your edits.
Be clear about what problem is occurring and how someone can recreate that problem or why your feature will help. Then be equally as clear about the steps you took to make your changes.
* It’s best to test. Run your changes against any existing tests if they exist and create new ones when needed. Whether tests exist or not, make sure your changes don’t break the existing project.
* Include screenshots of the before and after if your changes include differences in HTML/CSS. Drag and drop the images into the body of your pull request.
* Contribute in the style of the project to the best of your abilities. This may mean using indents, semi colons or comments differently than you would in your own repository, but makes it easier for the maintainer to merge, others to understand and maintain in the future.

### Open Pull Requests
Once you’ve opened a pull request a discussion will start around your proposed changes. Other contributors and users may chime in, but ultimately the decision is made by the maintainer(s). You may be asked to make some changes to your pull request, if so, add more commits to your branch and push them – they’ll automatically go into the existing pull request.

###Adding a screenshot
If your pull request is merged – great! If it is not, please communicate with the maintainer! it may not be what the project maintainer had in mind, or someone was already working on it. This happens, so our recommendation is to take any feedback you’ve received and go forth and pull request again!

