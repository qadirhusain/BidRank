package outbid.clone.submission.config;

import org.modelmapper.convention.MatchingStrategies;
import outbid.clone.submission.dto.SubmissionRequest;
import outbid.clone.submission.entity.Submission;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.typeMap(SubmissionRequest.class, Submission.class)
                .addMappings(mapper -> {

                    // These are controlled by the service/entity, never by the request
                    mapper.skip(Submission::setId);
                    mapper.skip(Submission::setStatus);
                    mapper.skip(Submission::setRejectionReason);
                    mapper.skip(Submission::setCreatedAt);
                    mapper.skip(Submission::setUpdatedAt);
                });

        return modelMapper;
    }
}