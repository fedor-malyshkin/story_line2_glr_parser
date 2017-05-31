package ru.nlp_project.story_line2.glr_parser.dagger;

import javax.inject.Singleton;

import dagger.Component;
import ru.nlp_project.story_line2.glr_parser.GLRParser;

@Singleton
@Component(modules = GLRParserModule.class)
public abstract class GLRParserComponent {

	public abstract void inject(GLRParser parser);
}
