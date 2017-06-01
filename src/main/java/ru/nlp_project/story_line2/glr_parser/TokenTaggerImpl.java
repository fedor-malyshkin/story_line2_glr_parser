package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import ru.nlp_project.story_line2.config.ConfigurationException;
import ru.nlp_project.story_line2.glr_parser.IConfigurationManager.MasterConfiguration;
import ru.nlp_project.story_line2.glr_parser.Token.Lexeme;
import ru.nlp_project.story_line2.morph.GrammemeEnum;
import ru.nlp_project.story_line2.morph.hmm_pos_tagger.HMMPOSTagger;
import ru.nlp_project.story_line2.morph.hmm_pos_tagger.HMMPOSTaggerDB;
import ru.nlp_project.story_line2.morph.hmm_pos_tagger.HMMPOSTaggerDBReader;

public class TokenTaggerImpl implements ITokenTagger {
	@Inject
	public IConfigurationManager configurationManager;
	private boolean active = false;
	private HMMPOSTagger tagger;

	@Inject
	public TokenTaggerImpl() {
		super();
	}


	@Override
	public void processTokens(List<Token> tokens) {
		// if !active -- do nothing
		if (!active)
			return;
		List<String> observations = tokens.stream().filter(t -> t.kwWord == true).map(t -> t.value)
				.collect(Collectors.toList());
		List<GrammemeEnum> grammemes = tagger.forwardViterbi(observations);
		List<Token> toProcess =
				tokens.stream().filter(t -> t.kwWord == true).collect(Collectors.toList());
		IntStream.range(0, observations.size()).forEach(i -> {
			Token token = toProcess.get(i);
			GrammemeEnum pos = grammemes.get(i);

			boolean hasPos = false;
			ListIterator<Lexeme> iterator = token.getLexemesIterator();
			while (iterator.hasNext()) {
				Lexeme lexeme = iterator.next();
				if (lexeme.getPOS() == pos) {
					hasPos = true;
					break;
				}
			}

			// если такой части речи нет -- ничего не трогаем и рассматриваем следующий токен
			if (!hasPos)
				return;

			iterator = token.getLexemesIterator();
			while (iterator.hasNext()) {
				Lexeme lexeme = iterator.next();
				if (lexeme.getPOS() != pos)
					iterator.remove();
			}
		});
	}


	@Override
	public void initialize() {
		MasterConfiguration configuration = configurationManager.getMasterConfiguration();
		if (configuration.taggerJsonDB == null || configuration.taggerJsonDB.isEmpty()) {
			active = false;
			return;
		}

		try {
			InputStream is = configurationManager.getSiblingInputStream(configuration.taggerJsonDB);
			HMMPOSTaggerDBReader dbReader = new HMMPOSTaggerDBReader();
			HMMPOSTaggerDB taggerDB = dbReader.read(is);
			tagger = new HMMPOSTagger(taggerDB);
			active = true;
		} catch (ConfigurationException | IOException e) {
			throw new IllegalStateException(e);
		}

	}

}
