#! groovy
timestamps {
	// node('((linux && vncserver) || osx) && jdk') {
	node('linux && vncserver && jdk') {
		try {
			stage('Checkout') {
				checkout scm
			}

			stage('Dependencies') {
				step([$class: 'CopyArtifact',
					filter: 'repository/',
					fingerprintArtifacts: true,
					projectName: "../libraries_com/tycho",
					target: 'libraries_com'])
					// Hack the target definition to point to libraries_com repo
					targetFile = 'releng/com.aptana.studio.target/com.aptana.studio.target.target'
					find = 'file:/Users/cwilliams/repos/libraries_com/releng/com.aptana.ide.libraries.subscription.update/target/repository'.replaceAll('/', '\\\\/')
					replace = "file:${pwd()}/libraries_com/repository".replaceAll('/', '\\\\/')
					// FIXME sed -i doesn't work right on macs...
					sh "sed -i 's/${find}/${replace}/g' ${targetFile}"
			}

			stage('Build') {
				withEnv(["PATH+MAVEN=${tool name: 'Maven 3.5.0', type: 'maven'}/bin"]) {
					wrap([$class: 'Xvnc', takeScreenshot: false, useXauthority: true]) {
						sh 'mvn clean verify'
					}
				}
				junit 'tests/*/target/surefire-reports/TEST-*.xml'
				dir('releng/com.aptana.studio.update/target') {
					// To keep backwards compatability with existing build pipeline, rename to "dist"
					sh 'mv repository dist'
					archiveArtifacts artifacts: 'dist/**/*'
					def jarName = sh(returnStdout: true, script: 'ls dist/features/com.aptana.feature_*.jar').trim()
					def version = (jarName =~ /.*?_(.+)\.jar/)[0][1]
					currentBuild.displayName = "#${version}-${currentBuild.number}"
				}
				dir('releng/com.aptana.studio.test.update/target') {
					sh 'mv repository dist-tests'
					archiveArtifacts artifacts: 'dist-tests/**/*'
				}
			}

			// If not a PR, trigger downstream builds for same branch
			if (!env.BRANCH_NAME.startsWith('PR-')) {
				build job: "appcelerator-studio/titanium_studio/${env.BRANCH_NAME}", wait: false
				build job: "../studio3-php/${env.BRANCH_NAME}", wait: false
				build job: "../studio3-ruby/${env.BRANCH_NAME}", wait: false
				build job: "../Pydev/${env.BRANCH_NAME}", wait: false
			}
		} catch (e) {
			// if any exception occurs, mark the build as failed
			currentBuild.result = 'FAILURE'
			throw e
		// } finally {
			// step([$class: 'WsCleanup', notFailBuild: true])
		}
	} // node
} // timestamps
